package com.m520it.test;

import com.m520it.domain.Article;
import com.m520it.repositories.ArticleRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class SpringDataCreateElasticsearch {

    @Autowired
    private ArticleRepository repository;
    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 创建一个索引库
     */
    @Test
    public void testCreatIndeice(){
        //根据javabean中的注解,创建索引库的信息
        template.createIndex(Article.class);
    }

    /**
     * 创建一个文档将其加入索引库中
     */
    @Test
    public void saveDocument(){
        for (int i = 0; i < 20; i++) {
            Article article=new Article();
            article.setId(i);
            article.setTitle("本篇文章中Spring版本使用的是版本"+i);
            article.setContent("节点客户端以无数据节点(none data node)身份加入集群，换言之，它自己不存储任何数据，但是它知道数据在集群中的具体位置，并且能够直接转发请求到对应的节点上。");
            //把文档写入索引库
            repository.save(article);
        }
    }

    /**
     * 删除文档
     *     *根据id进行删除
     */
    @Test
    public void testDeleteById(){
        //删除文档,根据id进行删除
        repository.deleteById(1l);
    }

    /**
     * 查询所有的文档的信息
     */
    @Test
    public void testFindAll(){
        //查询出所有的文档的信息
        Iterable<Article> iterable = repository.findAll();
        //将所有的文档的信息迭代出来
        Iterator<Article> iterator = iterable.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
    /**
     * 查询单个的文档的信息
     *     *根据id进行查询
     */
    @Test
    public void testQueryById(){
        Optional<Article> optional = repository.findById(3l);
        System.out.println(optional.get());

        //分页查询
        Page<Article> page = repository.findAll(PageRequest.of(0, 10));
        List<Article> list = page.getContent();
        for (Article article : list) {
            System.out.println(article);
        }
    }

    /**
     * 根据title的内容,按照给定的字符串进行查询
     *       *默认值返回十条数据
     */
    @Test
    public void testQueryTitle(){
        List<Article> list = repository.findByTitle("spring");
        for (Article article : list) {
            System.out.println(article);
        }
    }

    /**
     * 根据给定的讴歌条件进行查询
     *    1.title:  标题区
     *    2.content:  内容区
     */
    @Test
    public void testQueryTitleOrContent(){
        List<Article> list=repository.findByTitleOrContent("spring","客户端");
        for (Article article : list) {
            System.out.println(article);
        }
    }

    /**
     * 将查询的结果进行分页,按照自己给定的信息进行分页
     */
    @Test
    public void testpage(){
        Pageable pageable=PageRequest.of(1,15);
        List<Article> list=repository.findByTitleOrContent("spring","客户端",pageable);
        for (Article article : list) {
            System.out.println(article);
        }
    }

    /**
     * 使用原生的方式对文档的信息进行查询
     *    *功能比较强,查询的字符串可以分词,并且无须连续,也可以查询查询出来
     *
     */
    @Test
    public void testNativeSearchQuery(){
        NativeSearchQuery query= new NativeSearchQueryBuilder()
                //构建查询的条件
                .withQuery(QueryBuilders.queryStringQuery("spring").defaultField("title"))
                //查询的结果进行分页
                .withPageable(PageRequest.of(0,15))
                //构建查询
                .build();
        List<Article> list = template.queryForList(query, Article.class);
        for (Article article : list) {
            System.out.println(article);
        }
    }
}
