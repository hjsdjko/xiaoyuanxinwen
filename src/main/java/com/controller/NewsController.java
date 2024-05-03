
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 新闻
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/news")
public class NewsController {
    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    private static final String TABLE_NAME = "news";

    @Autowired
    private NewsService newsService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ForumService forumService;//论坛交流
    @Autowired
    private GonggaoService gonggaoService;//公告信息
    @Autowired
    private NewsCollectionService newsCollectionService;//新闻收藏
    @Autowired
    private NewsLiuyanService newsLiuyanService;//新闻留言
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = newsService.queryPage(params);

        //字典表数据转换
        List<NewsView> list =(List<NewsView>)page.getList();
        for(NewsView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        NewsEntity news = newsService.selectById(id);
        if(news !=null){
            //entity转view
            NewsView view = new NewsView();
            BeanUtils.copyProperties( news , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody NewsEntity news, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,news:{}",this.getClass().getName(),news.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<NewsEntity> queryWrapper = new EntityWrapper<NewsEntity>()
            .eq("news_name", news.getNewsName())
            .eq("news_video", news.getNewsVideo())
            .eq("zan_number", news.getZanNumber())
            .eq("cai_number", news.getCaiNumber())
            .eq("news_types", news.getNewsTypes())
            .eq("news_erji_types", news.getNewsErjiTypes())
            .eq("shangxia_types", news.getShangxiaTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        NewsEntity newsEntity = newsService.selectOne(queryWrapper);
        if(newsEntity==null){
            news.setZanNumber(1);
            news.setCaiNumber(1);
            news.setNewsClicknum(1);
            news.setShangxiaTypes(1);
            news.setInsertTime(new Date());
            news.setCreateTime(new Date());
            newsService.insert(news);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody NewsEntity news, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,news:{}",this.getClass().getName(),news.toString());
        NewsEntity oldNewsEntity = newsService.selectById(news.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        if("".equals(news.getNewsPhoto()) || "null".equals(news.getNewsPhoto())){
                news.setNewsPhoto(null);
        }
        if("".equals(news.getNewsFile()) || "null".equals(news.getNewsFile())){
                news.setNewsFile(null);
        }
        if("".equals(news.getNewsVideo()) || "null".equals(news.getNewsVideo())){
                news.setNewsVideo(null);
        }
        if("".equals(news.getNewsContent()) || "null".equals(news.getNewsContent())){
                news.setNewsContent(null);
        }

            newsService.updateById(news);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<NewsEntity> oldNewsList =newsService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        newsService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<NewsEntity> newsList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            NewsEntity newsEntity = new NewsEntity();
//                            newsEntity.setNewsName(data.get(0));                    //新闻名称 要改的
//                            newsEntity.setNewsUuidNumber(data.get(0));                    //新闻编号 要改的
//                            newsEntity.setNewsPhoto("");//详情和图片
//                            newsEntity.setNewsFile(data.get(0));                    //附件 要改的
//                            newsEntity.setNewsVideo(data.get(0));                    //视频 要改的
//                            newsEntity.setZanNumber(Integer.valueOf(data.get(0)));   //赞 要改的
//                            newsEntity.setCaiNumber(Integer.valueOf(data.get(0)));   //踩 要改的
//                            newsEntity.setNewsTypes(Integer.valueOf(data.get(0)));   //新闻类型 要改的
//                            newsEntity.setNewsErjiTypes(Integer.valueOf(data.get(0)));   //二级类型 要改的
//                            newsEntity.setNewsClicknum(Integer.valueOf(data.get(0)));   //新闻热度 要改的
//                            newsEntity.setNewsContent("");//详情和图片
//                            newsEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否上架 要改的
//                            newsEntity.setInsertTime(date);//时间
//                            newsEntity.setCreateTime(date);//时间
                            newsList.add(newsEntity);


                            //把要查询是否重复的字段放入map中
                                //新闻编号
                                if(seachFields.containsKey("newsUuidNumber")){
                                    List<String> newsUuidNumber = seachFields.get("newsUuidNumber");
                                    newsUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> newsUuidNumber = new ArrayList<>();
                                    newsUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("newsUuidNumber",newsUuidNumber);
                                }
                        }

                        //查询是否重复
                         //新闻编号
                        List<NewsEntity> newsEntities_newsUuidNumber = newsService.selectList(new EntityWrapper<NewsEntity>().in("news_uuid_number", seachFields.get("newsUuidNumber")));
                        if(newsEntities_newsUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(NewsEntity s:newsEntities_newsUuidNumber){
                                repeatFields.add(s.getNewsUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [新闻编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        newsService.insertBatch(newsList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }



    /**
    * 个性推荐
    */
    @IgnoreAuth
    @RequestMapping("/gexingtuijian")
    public R gexingtuijian(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("gexingtuijian方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        CommonUtil.checkMap(params);
        List<NewsView> returnNewsViewList = new ArrayList<>();

        //查看收藏
        Map<String, Object> params1 = new HashMap<>(params);params1.put("sort","id");params1.put("yonghuId",request.getSession().getAttribute("userId"));
        params1.put("shangxiaTypes",1);
        params1.put("newsYesnoTypes",2);
        PageUtils pageUtils = newsCollectionService.queryPage(params1);
        List<NewsCollectionView> collectionViewsList =(List<NewsCollectionView>)pageUtils.getList();
        Map<Integer,Integer> typeMap=new HashMap<>();//购买的类型list
        for(NewsCollectionView collectionView:collectionViewsList){
            Integer newsTypes = collectionView.getNewsTypes();
            if(typeMap.containsKey(newsTypes)){
                typeMap.put(newsTypes,typeMap.get(newsTypes)+1);
            }else{
                typeMap.put(newsTypes,1);
            }
        }
        List<Integer> typeList = new ArrayList<>();//排序后的有序的类型 按最多到最少
        typeMap.entrySet().stream().sorted((o1, o2) -> o2.getValue() - o1.getValue()).forEach(e -> typeList.add(e.getKey()));//排序
        Integer limit = Integer.valueOf(String.valueOf(params.get("limit")));
        for(Integer type:typeList){
            Map<String, Object> params2 = new HashMap<>(params);params2.put("newsTypes",type);
            params2.put("shangxiaTypes",1);
            params2.put("newsYesnoTypes",2);
            PageUtils pageUtils1 = newsService.queryPage(params2);
            List<NewsView> newsViewList =(List<NewsView>)pageUtils1.getList();
            returnNewsViewList.addAll(newsViewList);
            if(returnNewsViewList.size()>= limit) break;//返回的推荐数量大于要的数量 跳出循环
        }
        params.put("shangxiaTypes",1);
        params.put("newsYesnoTypes",2);
        //正常查询出来商品,用于补全推荐缺少的数据
        PageUtils page = newsService.queryPage(params);
        if(returnNewsViewList.size()<limit){//返回数量还是小于要求数量
            int toAddNum = limit - returnNewsViewList.size();//要添加的数量
            List<NewsView> newsViewList =(List<NewsView>)page.getList();
            for(NewsView newsView:newsViewList){
                Boolean addFlag = true;
                for(NewsView returnNewsView:returnNewsViewList){
                    if(returnNewsView.getId().intValue() ==newsView.getId().intValue()) addFlag=false;//返回的数据中已存在此商品
                }
                if(addFlag){
                    toAddNum=toAddNum-1;
                    returnNewsViewList.add(newsView);
                    if(toAddNum==0) break;//够数量了
                }
            }
        }else {
            returnNewsViewList = returnNewsViewList.subList(0, limit);
        }

        for(NewsView c:returnNewsViewList)
            dictionaryService.dictionaryConvert(c, request);
        page.setList(returnNewsViewList);
        return R.ok().put("data", page);
    }

    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = newsService.queryPage(params);

        //字典表数据转换
        List<NewsView> list =(List<NewsView>)page.getList();
        for(NewsView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Integer id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        NewsEntity news = newsService.selectById(id);
            if(news !=null){

                //点击数量加1
                news.setNewsClicknum(news.getNewsClicknum()+1);
                newsService.updateById(news);

                //entity转view
                NewsView view = new NewsView();
                BeanUtils.copyProperties( news , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody NewsEntity news, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,news:{}",this.getClass().getName(),news.toString());
        Wrapper<NewsEntity> queryWrapper = new EntityWrapper<NewsEntity>()
            .eq("news_name", news.getNewsName())
            .eq("news_uuid_number", news.getNewsUuidNumber())
            .eq("news_video", news.getNewsVideo())
            .eq("zan_number", news.getZanNumber())
            .eq("cai_number", news.getCaiNumber())
            .eq("news_types", news.getNewsTypes())
            .eq("news_erji_types", news.getNewsErjiTypes())
            .eq("news_clicknum", news.getNewsClicknum())
            .eq("shangxia_types", news.getShangxiaTypes())
//            .notIn("news_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        NewsEntity newsEntity = newsService.selectOne(queryWrapper);
        if(newsEntity==null){
                news.setZanNumber(1);
                news.setCaiNumber(1);
            news.setNewsClicknum(1);
            news.setInsertTime(new Date());
            news.setCreateTime(new Date());
        newsService.insert(news);

            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

}

