package com.robot.utils;

import com.duangframework.db.mongodb.MongoDao;
import com.duangframework.db.mongodb.MongodbConnectOptions;
import com.duangframework.db.mongodb.MongodbDbClient;
import com.mongodb.MongoClient;
import com.robot.entity.Logs;
import com.robot.mvc.exceptions.RobotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DbKit {

    private static final Logger LOG = LoggerFactory.getLogger(DbKit.class);

    private static DbKit dbKit = new DbKit();
    private static Lock lock = new ReentrantLock();
    private static MongoDao<Logs> logsDao = null;
    private static MongodbDbClient mongodbDbClient = null;

    public static DbKit duang() {
        try {
            lock.lock();
            if (null == mongodbDbClient) {
                mongodbDbClient = new MongodbDbClient(new MongodbConnectOptions.Builder()
                        .dataBase("robot")
                        .host("192.168.8.210")
                        .port(27017)
                        .userName("admin")
                        .passWord("1b88ab6d")
                        .build());
                MongoClient client = mongodbDbClient.getClient();
                logsDao = new MongoDao<Logs>(mongodbDbClient.getClientId(), Logs.class);
            }
            return dbKit;
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private DbKit() {

    }

    public Logs saveLogs(Logs logs) {
        try {
            return logsDao.save(logs);
        } catch (Exception e) {
            throw new RobotException("保存日志时出错:" + e.getMessage(), e);

        }
    }

}
