package com.m520it.es;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ElasticSearch_query {

    private TransportClient client;

    @Before
    public void init() throws UnknownHostException {
        //1.创建一个Settings的对象
        Settings settings=Settings.builder()
                .put("cluster.name","my-elasticsearch")
                .build();
        //2.创建一个client的对象
        client=new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));

    }

    /**
     * 进行查询,根据id尽心查询
     */
    @Test
    public void testQueryById(){
        //1.创建一个client的对象
        //2.创建一个querybuilder的对象,创建查询的条件,根据什么来进行查询
        QueryBuilder builder= QueryBuilders.idsQuery().addIds("1","2");
        //3.执行查询
        //3.1查询的索引库
        SearchResponse searchResponse = client.prepareSearch("index_hello")
                //3.2根据哪个type进行查询
                .setTypes("article")
                //3.3查询的条件,加入查询的条件
                .setQuery(builder)
                //执行查询
                .get();
        //4,取查询的结果,也就是命中数
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询结果的总记录数:"+hits.getTotalHits());
        //查询结果的列表
        Iterator<SearchHit> iterator = hits.iterator();
        while(iterator.hasNext()){
            SearchHit next = iterator.next();
            //打印文档对象,以json的格式输出
            System.out.println(next.getSourceAsString());
            //取文档的属性
            Map<String, Object> source = next.getSource();
            Set<String> strings = source.keySet();
            //迭代文档的内容
            for (String string : strings) {
                Object o = source.get(string);
                System.out.println(o);
            }
        }
        //关闭client
        client.close();
    }

    /**
     * 封装查询的方法
     * @param builder
     */
    private void  querySearch(QueryBuilder builder){
        //创建查询
        SearchResponse searchResponse = client.prepareSearch("index_hello")
                .setTypes("article")
                //进行分页的查询
                //从第几页开始查询
                .setFrom(0)
                //每页查询的数目
                .setSize(5)
                .setQuery(builder)
                .get();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询到的数目:"+hits.getTotalHits());
        Iterator<SearchHit> iterator = hits.iterator();
        while(iterator.hasNext()){
            SearchHit searchHit = iterator.next();
            String jsonSource = searchHit.getSourceAsString();
            System.out.println(jsonSource);
            Map<String, Object> source = searchHit.getSource();
            Set<String> set = source.keySet();
            for (String str : set) {
                Object o = source.get(str);
                System.out.println(o);
            }
        }
    }

    /**
     * 设置高亮显示
     * @param builder  :查询的条件的设置
     * @param hightlightField  :高亮显示的域
     */
    private void  querySearch(QueryBuilder builder ,String hightlightField){
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        //设置高亮显示的字段
        highlightBuilder.field(hightlightField);
        //设置高亮显示的前缀
        highlightBuilder.preTags("<em>");
        //设置高亮显示的后缀
        highlightBuilder.postTags("</em>");
        //创建查询
        SearchResponse searchResponse = client.prepareSearch("index_hello")
                .setTypes("article")
                //进行分页的查询
                //从第几页开始查询
                .setFrom(0)
                //每页查询的数目
                .setSize(5)
                //设置高亮显示的信息
                .highlighter(highlightBuilder)
                //设置查询的条件
                .setQuery(builder)
                //执行查询
                .get();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询到的数目:"+hits.getTotalHits());
        Iterator<SearchHit> iterator = hits.iterator();
        while(iterator.hasNext()){
            SearchHit searchHit = iterator.next();
            String jsonSource = searchHit.getSourceAsString();
            System.out.println(jsonSource);

            System.out.println("*********************高亮的结果*******begin");
            //从结果集中获取高亮显示的结果集,封装到map的集合中
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            System.out.println(highlightFields);
            //获取指定的搜索域(字段)的结果
            HighlightField highlightField = highlightFields.get(hightlightField);
            //获取结果域中所有的文本结果
            Text[] fragments = highlightField.getFragments();
            System.out.println(Arrays.toString(fragments));
            if (fragments!=null){
                String str = fragments[0].toString();
                System.out.println(str);
            }
            System.out.println("******************高亮的结果************end");
            //将所有的查询的结果封装到map的集合中
            Map<String, Object> source = searchHit.getSource();
            //迭代出所有的结果集
            Set<String> set = source.keySet();
            for (String str : set) {
                Object o = source.get(str);
                System.out.println(o);
            }
        }
    }
    /**
     *根据term进行查询:关键字查询
     */

    @Test
    public void testQueryByTerm(){
        //创建一个querybuilt的对象
        /**
         * 参数1:要搜索的字段
         * 参数2:查询的条件:字符串
         */
        QueryBuilder builder=QueryBuilders.termQuery("title","这是");
        querySearch(builder,"title");
    }

    /**
     * 根据查询条件先分词,然后根据分词的结果进行查询
     */
    @Test
    public void testQueryString(){
        //创建QueryBuilder的对象
        /**
         * 第一个条件:创建搜索的条件,根据什么来进行查询
         * 第二个条件:默认的搜索域:在哪里进行所搜
         */
        QueryBuilder builder=QueryBuilders.queryStringQuery("spring").defaultField("title");
        querySearch(builder);
    }

}
