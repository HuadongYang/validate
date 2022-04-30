package com.java.validate.jdk.lock;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class OptimisticLock {
    private JdbcTemplate jdbcTemplate;

    class User {
        private int id;
        private String name;
        private int version;
        private int money;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getMoney() {
            return money;
        }

        public void setMoney(int money) {
            this.money = money;
        }

        public User(int id, String name, int version) {
            this.id = id;
            this.name = name;
            this.version = version;
        }
    }


    /**
     * 使用版本号来加乐观锁
     */
    public void lock1() {
//        int id = 1;
//        User user = jdbcTemplate.query("select * from user where id=" + id, resultSet -> {
//            return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3));
//        });
//        String newName = "huanhuansb";
//        int oldVersion = user.getVersion();
//        int newVersion = oldVersion + 1;
//        int oldMoney = user.getMoney();
//
//        if (oldMoney <= 0) {
//
//        }
//        int newMoney = oldMoney - 1;
//
//
//        int update = jdbcTemplate.update("update user set money=" + newMoney + ",version=" + newVersion + " where id=" + id + " and version = " + oldVersion);
    }

    private Unsafe unsafe = Unsafe.getUnsafe();
    private int lock2V = 0;
    private static final Lock lock = new ReentrantLock();

    /**
     * jdk提供的unsafe下的cas来加乐观锁
     *
     * @throws NoSuchFieldException
     */
    public void lock2() throws NoSuchFieldException {
        lock.lock();
        lock.unlock();


        long lock2VOffset = unsafe.objectFieldOffset(OptimisticLock.class.getDeclaredField("lock2V"));
        boolean isSuccess = unsafe.compareAndSwapInt(this, lock2VOffset, 0, 1);
        unsafe.compareAndSwapInt(this, lock2VOffset, 1, 0);
        if (isSuccess) {
            System.out.println("get lock success");
        }
    }

    private volatile static int state = 0;
    private volatile static String stateThread;

    private static List<Thread> queues = new ArrayList<>();

    public void lock() throws InterruptedException, NoSuchFieldException {
        if (state != 0) {
            queues.add(Thread.currentThread());
            Thread.currentThread().wait();
            tryLock();
        } else {
            tryLock();
        }
    }

    private void tryLock() throws InterruptedException, NoSuchFieldException {
        long lock2VOffset = unsafe.objectFieldOffset(OptimisticLock.class.getDeclaredField("lock2V"));
        if (unsafe.compareAndSwapInt(this, lock2VOffset, 0, 1)) {
            stateThread = Thread.currentThread().getName();
        } else {
            queues.add(Thread.currentThread());
            Thread.currentThread().wait();
            tryLock();
        }
    }

    public void unLock() {
        if (state == 1 && stateThread.equals(Thread.currentThread().getName())) {
            stateThread = null;
            unsafe.compareAndSwapInt(this, state, 1, 0);
            if (!CollectionUtils.isEmpty(queues)) {
                Thread first = queues.remove(0);
                first.notify();
            }
        }
    }

    public static void main1(String[] args) {
        new Thread(() -> {
            OptimisticLock optimisticLock = new OptimisticLock();
            try {
                optimisticLock.lock();
                //业务逻辑1
            } catch (InterruptedException | NoSuchFieldException e) {
                e.printStackTrace();
            } finally {
                optimisticLock.unLock();
            }
        }).start();

        new Thread(() -> {
            OptimisticLock optimisticLock = new OptimisticLock();
            try {
                optimisticLock.lock();
                //业务逻辑2
            } catch (InterruptedException | NoSuchFieldException e) {
                e.printStackTrace();
            } finally {
                optimisticLock.unLock();
            }
        }).start();
    }

    public static void main(String[] args) {
        new Thread(()->{
            Thread.currentThread().setName("thread-1");
            lock.lock();
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }).start();
        new Thread(()->{
            Thread.currentThread().setName("thread-2");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                lock.lock();
            } finally {
                lock.unlock();
            }

        }).start();
    }

}
