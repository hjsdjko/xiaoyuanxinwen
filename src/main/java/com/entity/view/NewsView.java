package com.entity.view;

import org.apache.tools.ant.util.DateUtils;
import com.annotation.ColumnInfo;
import com.entity.NewsEntity;
import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import com.utils.DateUtil;

/**
* 新闻
* 后端返回视图实体辅助类
* （通常后端关联的表或者自定义的字段需要返回使用）
*/
@TableName("news")
public class NewsView extends NewsEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	//当前表
	/**
	* 新闻类型的值
	*/
	@ColumnInfo(comment="新闻类型的字典表值",type="varchar(200)")
	private String newsValue;
	/**
	* 二级类型的值
	*/
	@ColumnInfo(comment="二级类型的字典表值",type="varchar(200)")
	private String newsErjiValue;
	/**
	* 是否上架的值
	*/
	@ColumnInfo(comment="是否上架的字典表值",type="varchar(200)")
	private String shangxiaValue;




	public NewsView() {

	}

	public NewsView(NewsEntity newsEntity) {
		try {
			BeanUtils.copyProperties(this, newsEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	//当前表的
	/**
	* 获取： 新闻类型的值
	*/
	public String getNewsValue() {
		return newsValue;
	}
	/**
	* 设置： 新闻类型的值
	*/
	public void setNewsValue(String newsValue) {
		this.newsValue = newsValue;
	}
	//当前表的
	/**
	* 获取： 二级类型的值
	*/
	public String getNewsErjiValue() {
		return newsErjiValue;
	}
	/**
	* 设置： 二级类型的值
	*/
	public void setNewsErjiValue(String newsErjiValue) {
		this.newsErjiValue = newsErjiValue;
	}
	//当前表的
	/**
	* 获取： 是否上架的值
	*/
	public String getShangxiaValue() {
		return shangxiaValue;
	}
	/**
	* 设置： 是否上架的值
	*/
	public void setShangxiaValue(String shangxiaValue) {
		this.shangxiaValue = shangxiaValue;
	}




	@Override
	public String toString() {
		return "NewsView{" +
			", newsValue=" + newsValue +
			", newsErjiValue=" + newsErjiValue +
			", shangxiaValue=" + shangxiaValue +
			"} " + super.toString();
	}
}
