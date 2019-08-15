package zhang.xiulu.com.annotationusage;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private DBHelper mDBHelper;
    private SQLiteDatabase mSqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text).setOnClickListener(v -> {
//                handleEvent();
            getClassName(getPackageName());
        });
        mDBHelper = new DBHelper(this);
        mSqLiteDatabase = mDBHelper.getWritableDatabase();
        findViewById(R.id.insert).setOnClickListener(v->{
            insert();
        });

        findViewById(R.id.query).setOnClickListener(v -> {
            query();
        });
    }

    /**
     * 获取方法中的注解演示
     */
    private void handleEvent() {
        UseCase useCase = new UseCase();
        useCase.testRecord(this);
        Class clazz = useCase.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(LogRecord.class)) {
                //获取方法中的注解
                LogRecord logRecord = method.getAnnotation(LogRecord.class);
                if (logRecord != null) {
                    Log.e(logRecord.tag(), logRecord.msg());
                }
            }
        }
    }


    /**
     * 获取当前包名下所有类中含有特定注解的演示
     * 获取字段中的注解演示
     *
     * @param packageName
     * @return
     */
    public List<String > getClassName(String packageName){
        StringBuilder stateSql = new StringBuilder();
        stateSql.append("CREATE TABLE IF NOT EXISTS ");
        List<String >classNameList=new ArrayList<String >();
        try {

            DexFile df = new DexFile(this.getPackageCodePath());
            PathClassLoader classLoader = (PathClassLoader) Thread.currentThread().getContextClassLoader();
            //获取df中的元素 这里包含了所有可执行的类名 该类名包含了包名+类名的方式
            Enumeration<String> enumeration = df.entries();
            while (enumeration.hasMoreElements()) {//遍历
                String className = (String) enumeration.nextElement();
                Log.d(TAG, "getClassName:" + className);
                if (className.contains(packageName)) {
                    classNameList.add(className);
                    Class<?> clazz = df.loadClass(className, classLoader);
                    //判断类的声明中是否包含Table标签
                    if (!clazz.isAnnotationPresent(Table.class)) continue;
                    if (clazz != null) {
                        Table table = clazz.getAnnotation(Table.class);
                        if (table != null) {
                            Log.e(TAG, "getClassTable:" + table.value());
                            stateSql.append(table.value());
                        }
                        stateSql.append(" ( ");

                        //获取类中字段的注解
                        Field[] fields = clazz.getDeclaredFields();
                        for (Field field: fields) {
                            boolean isPresent = field.isAnnotationPresent(Column.class);
                            if (!isPresent) continue;
                            Column column = field.getAnnotation(Column.class);
                            String columnValue = column.value();
                            Log.d(TAG, "column:" + columnValue);
                            //判断字段类型
                            if (field.getType() == Integer.class || field.getType() == int.class) {
                                Log.d(TAG, "is Integer");
                                stateSql.append(columnValue + " INTEGER PRIMARY KEY AUTOINCREMENT,");
                            } else if (field.getType() == String.class) {
                                Log.d(TAG, "is String");
                                stateSql.append(columnValue + " TEXT,");
                            }

                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stateSql.replace(stateSql.length() -1, stateSql.length() , ");");
        Log.e(TAG, stateSql.toString());
        mDBHelper.createTable(stateSql.toString());
        return  classNameList;
    }


    private void insert() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", "1");
        contentValues.put("password", "admin");
        contentValues.put("username", "admin");
        mSqLiteDatabase.insertWithOnConflict("user", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        contentValues.put("id", "2");
        contentValues.put("password", "123456");
        contentValues.put("username", "zhangsan");
        mSqLiteDatabase.insertWithOnConflict("user", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void query() {
        Cursor cursor = mSqLiteDatabase.query("user",null, null,null,null,null,null);
        try{
            while (cursor != null && cursor.moveToNext()) {
                User user = new User();
                user.setId(cursor.getInt(0));
                user.setPassword(cursor.getString(1));
                user.setUsername(cursor.getString(2));
                Log.e(TAG, "query User:" + user);
            }
        }catch (SQLException ex) {

        }
    }

}
