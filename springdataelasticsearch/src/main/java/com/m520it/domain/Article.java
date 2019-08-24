package com.m520it.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * indexName:创建索引库
 * type:type(表)的名字
 */
@Document(indexName = "index_article",type = "article")
public class Article implements Serializable {

    @Id    //表明这是一个主键id的列
    /**
     * type:表示参数的类型
     * store:是否存储
     * analyzer:分词
     */
    @Field(type = FieldType.Long,store = true)
    private long id;
    @Field(type = FieldType.text,store = true,analyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.text,store = true,analyzer = "ik_smart")
    private String content;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
