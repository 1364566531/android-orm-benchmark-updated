package com.littleinc.orm_benchmark.squeaky;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.littleinc.orm_benchmark.BenchmarkExecutable;
import com.littleinc.orm_benchmark.util.Util;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.stmt.Where;
import co.touchlab.squeaky.table.TableUtils;

/**
 * Created by kgalligan on 10/12/15.
 */
public class SqueakyExecutor implements BenchmarkExecutable
{

    private static final String TAG = "SqueakyExecutor";

    private DataBaseHelper mHelper;

    @Override
    public void init(Context context, boolean useInMemoryDb) {
        Log.d(TAG, "Creating DataBaseHelper");
        DataBaseHelper.init(context, useInMemoryDb);
        mHelper = DataBaseHelper.getInstance();
    }

    @Override
    public long createDbStructure() throws SQLException
    {
        long start = System.nanoTime();
        co.touchlab.squeaky.table.TableUtils.createTables(mHelper.getWritableDatabase(), User.class, Message.class);
        return System.nanoTime() - start;
    }

    @Override
    public long writeWholeData() throws SQLException {
        List<User> users = new LinkedList<User>();
        for (int i = 0; i < NUM_USER_INSERTS; i++) {
            User newUser = new User();
            newUser.setLastName(Util.getRandomString(10));
            newUser.setFirstName(Util.getRandomString(10));

            users.add(newUser);
        }

        List<Message> messages = new LinkedList<Message>();
        for (int i = 0; i < NUM_MESSAGE_INSERTS; i++) {
            Message newMessage = new Message();
            newMessage.setCommandId(i);
            newMessage.setSortedBy(System.nanoTime());
            newMessage.setContent(Util.getRandomString(100));
            newMessage.setClientId(System.currentTimeMillis());
            newMessage
                    .setSenderId(Math.round(Math.random() * NUM_USER_INSERTS));
            newMessage
                    .setChannelId(Math.round(Math.random() * NUM_USER_INSERTS));
            newMessage.setCreatedAt((int) (System.currentTimeMillis() / 1000L));

            messages.add(newMessage);
        }

        long start = System.nanoTime();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        db.beginTransaction();

        try {
            Dao userDao = mHelper.getDao(User.class);
            for (User user : users) {
                userDao.createOrUpdate(user);
            }
            Log.d(TAG, "Done, wrote " + NUM_USER_INSERTS + " users");

            Dao messageDao = mHelper.getDao(Message.class);
            for (Message message : messages) {
                messageDao.createOrUpdate(message);
            }
            Log.d(TAG, "Done, wrote " + NUM_MESSAGE_INSERTS + " messages");

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return System.nanoTime() - start;
    }

    @Override
    public long readWholeData() throws SQLException {
        long start = System.nanoTime();
        Log.d(TAG,
              "Read, " + mHelper.getDao(Message.class).queryForAll().size()
                      + " rows");
        return System.nanoTime() - start;
    }

    @Override
    public long readIndexedField() throws SQLException {
        long start = System.nanoTime();
        Log.d(TAG,
              "Read, "
                      + mHelper
                      .getDao(Message.class)
                      .queryForEq(Message.COMMAND_ID,
                                  LOOK_BY_INDEXED_FIELD).size() + " rows");
        return System.nanoTime() - start;
    }

    @Override
    public long readSearch() throws SQLException {
//        SelectArg arg = new SelectArg("%" + SEARCH_TERM + "%");
        long start = System.nanoTime();
        /*Dao dao = mHelper.getDao(Message.class);
        Where where = dao.createWhere()
                            .like(Message.COMMAND_ID, "%" + SEARCH_TERM + "%");

        dao.query()
        Log.d(TAG,
              "Read, "
                      + mHelper.mHelper.getDao(Message.class).queryBuilder()
                               .limit(SEARCH_LIMIT).where()
                               .like(Message.CONTENT, arg).query().size()
                      + " rows");*/
        return System.nanoTime() - start;
    }

    @Override
    public long dropDb() throws SQLException {
        long start = System.nanoTime();
        TableUtils.dropTables(mHelper.getWritableDatabase(), true, User.class, Message.class);
        return System.nanoTime() - start;
    }

    @Override
    public String getOrmName() {
        return "Squeaky";
    }
}