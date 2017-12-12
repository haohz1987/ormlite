package com.hz.ormlite;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * 简单的CURD,更多用法参考text.txt
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv;
    private StringBuffer contentBuffer;
    private UserDao userDao;
    private Dao<UserBean, Integer> userDaos;
    private UserBean userBean;
    private ArticleDao articleDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);
        findViewById(R.id.btn_3).setOnClickListener(this);
        findViewById(R.id.btn_4).setOnClickListener(this);
        findViewById(R.id.btn_5).setOnClickListener(this);
        tv = findViewById(R.id.tv);
        contentBuffer = new StringBuffer();
        userDao = new UserDao(MainActivity.this);
        articleDao = new ArticleDao(MainActivity.this);

    }

    //显示所有数据
    public void allView() {
        contentBuffer.delete(0, contentBuffer.length());
        List<UserBean> userBeansList = userDao.queryAll();
        for (UserBean userBean : userBeansList) {
            // 从用户信息中取出关联的文章列表信息
            ForeignCollection<ArticleBean> articles = userBean.getArticles();
            Iterator<ArticleBean> iterator = articles.iterator();
            while (iterator.hasNext()) {
                ArticleBean article = iterator.next();
                contentBuffer.append(article.toString() + "\n");
            }
        }

        tv.setText(contentBuffer.toString());
    }

    @Override
    public void onClick(View view) {
        ((TextView) findViewById(R.id.tv)).setText("");
        switch (view.getId()) {
            //初始化两条数据
            case R.id.btn_1:
                // 添加用户数据
                UserBean userData = new UserBean("用户名", '1');
                userDao.insert(userData);
                // 添加文章数据
                ArticleBean articleData = new ArticleBean("Art_标题", "Art_内容", userData);
                articleDao.insert(articleData);

                userData = new UserBean("用户_02", '2');//, new Date(), "地点"
                userDao.insert(userData);
                articleData = new ArticleBean("Art_标题_02", "Art_内容_02", userData);
                articleDao.insert(articleData);

                allView();
                break;
            // 添加用户数据
            case R.id.btn_2:
                UserBean userData1 = new UserBean("用户名_3", '3');
                userDao.insert(userData1);
                // 添加文章数据
                ArticleBean articleData1 = new ArticleBean("Art_标题_3", "Art_内容_3", userData1);
                articleDao.insert(articleData1);
                allView();
                break;
            //查询
            case R.id.btn_3:
                // 从数据库中根据ID取出文章信息
                ArticleBean articleBean;
                if (null != articleDao.queryById(2)) {
                    articleBean = articleDao.queryById(2);
                    LogT.w(articleBean.toString());
                    // 根据取出的用户id查询用户信息

                    userBean = userDao.queryById(articleBean.getUser().getId());
                }

                contentBuffer.delete(0, contentBuffer.length());
                // 从用户信息中取出关联的文章列表信息
                if (null != userBean && null != userBean.getArticles()) {
                    ForeignCollection<ArticleBean> articles = userBean.getArticles();
                    for (ArticleBean article : articles) {
                        contentBuffer.append(article.toString() + "\n");
                    }
                }
                tv.setText(contentBuffer.toString());

                break;
            //删除-有指定文章的作者
            case R.id.btn_4:
                // 从数据库中根据ID取出文章信息
                if (null != articleDao.queryById(2)) {
                    articleBean = articleDao.queryById(2);
                    LogT.w(articleDao.queryById(2).toString());
                    // 根据取出的用户id查询用户信息
                    userBean = userDao.queryById(articleBean.getUser().getId());

                    // 从用户信息中取出关联的文章列表信息
                    ForeignCollection<ArticleBean> articlesDelete = userBean.getArticles();
                    for (ArticleBean articleDelete : articlesDelete) {
                        articleDao.delete(articleDelete);
                    }
                    userDao.delete(userBean);
                }

                allView();
                break;
            //按条件查询
            case R.id.btn_5:
                QueryBuilder<UserBean, Integer> queryBuilder = null;
                try {
                    userDaos = DatabaseHelper.getInstance(MainActivity.this).getDao(UserBean.class);
                    queryBuilder = userDaos.queryBuilder();
                    //1.单值查询
                    //   select * from tb_article where user_id = 1 and name = 'xxx' ;
/*
                    Where<UserBean, Integer> where = queryBuilder.where();
                    where.eq("id", 1);
                    where.and();
                    where.eq("name", "用户名");
*/

                    //2.或查询
                    //  select * from tb_article where ( user_id = 1 and name = 'xxx' )  or ( user_id = 2 and name = 'yyy' )  ;
                    Where<UserBean, Integer> where = queryBuilder.where();
                    where.or(
                            where.and(
                                    where.eq("id", 1), where.eq("name", "用户名")),
                            where.and(
                                    where.eq("id", 2), where.eq("name", "用户_02")));

                    contentBuffer.delete(0, contentBuffer.length());
                    if (null != where && null != where.query()) {
                        LogT.w(where.query().toString());
                        List<UserBean> userBeanList = where.query();
                        for (UserBean userbean : userBeanList) {
                            // 从用户信息中取出关联的文章列表信息
                            if (null != userbean && null != userbean.getArticles()) {
                                ForeignCollection<ArticleBean> articles = userbean.getArticles();
                                for (ArticleBean article : articles) {
                                    contentBuffer.append(article.toString() + "\n");
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                tv.setText(contentBuffer.toString());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userDao.closeDH();
        articleDao.closeDH();
    }
}
