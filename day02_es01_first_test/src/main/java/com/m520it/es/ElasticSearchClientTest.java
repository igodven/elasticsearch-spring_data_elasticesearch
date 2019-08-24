package com.m520it.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElasticSearchClientTest {

    @Test
    public void testCreatClient() throws UnknownHostException {
        //1.创建一个settings的对象,相当于是一个配置信息,主要配置集群的名称
        Settings settings=Settings.builder()
                //设置集群的名称
                .put("cluster.name","my-elasticsearch")
                .build();
        //2.创建一个客户端client的对象
        TransportClient client=new PreBuiltTransportClient(settings);
        /**
         * 表示在哪个服务器下的哪个端口进行索引库的创建
         *     防止一台服务器的宕机,造成索引库的丢失,所以需要多配置几台服务器
         */
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301));
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302));
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        //3.使用管理员的运行的方式,使用client对象创建一个索引库
        client.admin().indices().prepareCreate("index_hello")
                //执行操作
                .get();
        //4.关闭client的对象
        client.close();
    }

    @Test
    public void testCreatClient2() throws IOException {
        //1.创建settings的对象
        Settings settings=Settings.builder()
                .put("cluster.name","my-elasticsearch")
                .build();
        //2.创建client的对象
        TransportClient client=new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        //3.创建一个Mappings的信息/创建json格式的数据
        XContentBuilder builder= XContentFactory.jsonBuilder()
                .startObject()                                            //{
                    .startObject("article")                               //"article":{
                        .startObject("properties")                        //"properties":{
                            .startObject("id")                            //"id":{
                                .field("type","long")          //"type":"long",
                                .field("store",true)           //"type":true,
                            .endObject()                                   //},
                            .startObject("title")                           //"title":{
                                .field("type","text")           //"type":"text",
                                .field("store",true)            //"store":true,
                                .field("analyzer","ik_smart")   //"analyzer":"ik_smart"
                            .endObject()                                    //}
                            .startObject("content")                         //"content":{
                                .field("type","text")            //"type":"text",
                                .field("store",true)             //"store":true,
                                .field("analyzer","ik_smart")    //"analyzer":"ik_smart"
                            .endObject()                                    //}
                        .endObject()                                        //}
                    .endObject()                                            //}
                .endObject();                                               //}

        //4.使用客户端创建索引库,并且将mapping的type的信息添加到索引库中
        client.admin().indices().preparePutMapping("index_hello")
                //索引库所对应的type(表)名
                .setType("article")
                //索引库的mappings的信息的来源
                .setSource(builder)
                //执行操作
                .get();
        //5.关闭索引库
        client.close();
    }

    @Test
    public void insertDocumentToIndex() throws IOException {
        //1.创建一个Settings的对象
        Settings settings=Settings.builder()
                //设置集群的名称
                .put("cluster.name","my-elasticsearch")
                .build();
        //2.创建一个client的对象,用来指明对哪些es的集群做操作
        TransportClient client=new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        //3.创建一个文档的信息
        XContentBuilder builder=XContentFactory.jsonBuilder();
        builder.startObject()
                    .field("id","2")
                    .field("title","这是什么原理呀,我怎么看不见你呀?告诉我吧")
                    .field("content","这是一个文本的内容,好像是新创建的")
                .endObject();
        //4.将创建的文档的信息加入到索引库中
        client.prepareIndex()
                //设置到哪个索引库
                .setIndex("index_hello")
                //设置到哪个type(表)中
                .setType("article")
                //设置索引的id
                .setId("2")
                //文档的来源
                .setSource(builder)
                //执行操作
                .get();
        //5.关闭客户端
        client.close();
    }

    /**
     * 通过javabean的方式创建json的格式
     */
    @Test
    public void testInsertByJavaBean() throws JsonProcessingException, UnknownHostException {
        //1.创建Settings的对象
        Settings settings=Settings.builder()
                .put("cluster.name","my-elasticsearch")
                .build();
        //2.创建client的对象
        TransportClient client=new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        //3.创建一个Article的对象
        Article article=new Article();
        article.setContent("这是通过javabean的方式创建的对象,然后再转成json格式的字符串");
        article.setTitle("这是javabean的标题,第二种方式创建document的信息");
        article.setId(3);
        //将对象转化成json格式的字符串
        ObjectMapper mapper=new ObjectMapper();
        String str = mapper.writeValueAsString(article);
        client.prepareIndex("index_hello","article","3")
                .setSource(str, XContentType.JSON)
                .get();
        //4.关闭client的对象
        client.close();
    }

    @Test
    public void testAddDocument() throws JsonProcessingException, UnknownHostException {
        //1.创建Settings的对象
        Settings settings=Settings.builder()
                .put("cluster.name","my-elasticsearch")
                .build();
        //2.创建client的对象
        TransportClient client=new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9303));
        for (int i = 4; i < 100; i++) {
            //3.创建一个Article的对象
            Article article=new Article();
            article.setContent("Spring Security 是基于Spring 应用程序提供的声明式安全保护的安全框架。Spring Sercurity 提供了完整的安全性解决方案"+i);
            article.setTitle("它能够在Web请求级别和方法调用级别处理身份认证和授权，因为是基于Spring，所以Spring Security充分利用了依赖注入(Dependency injection DI) 和面向切面的技术。"+i);
            article.setId(i);
            //将对象转化成json格式的字符串
            ObjectMapper mapper=new ObjectMapper();
            String str = mapper.writeValueAsString(article);
            client.prepareIndex("index_hello","article",i+"")
                    .setSource(str, XContentType.JSON)
                    .get();
        }

        //4.关闭client的对象
        client.close();
    }
}
