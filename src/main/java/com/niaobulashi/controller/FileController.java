package com.niaobulashi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.niaobulashi.Utils.HttpTools;
import com.niaobulashi.common.dto.ResponseCode;
import com.niaobulashi.dao.SysFileInfoDao;
import com.niaobulashi.dao.TaskInfoDao;
import com.niaobulashi.model.SysFileInfo;
import com.niaobulashi.model.TaskInfo;
import com.niaobulashi.properties.GlobalProperties;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @program: spring-boot-learning
 * @description: 文件
 * @author:
 * @create: 2019-07-19 15:28
 */
@Controller
public class FileController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 默认大小 50M
     */
    public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024;

    @Autowired
    private SysFileInfoDao sysFileInfoDao;

    @Autowired
    private TaskInfoDao taskInfoDao;

    @Autowired
    private GlobalProperties globalProperties;

    /**
     * 文件上传页面
     *
     * @return
     */
    @GetMapping("/")
    public String updatePage() {
        return "file";
    }


    @GetMapping("/showTasks")
    @ResponseBody
    public JSONObject showTask(){
        List<TaskInfo> all = taskInfoDao.findAll();

        Map<String, Object> stringJSONArrayHashMap = new HashMap<>();

        JSONObject data= new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (TaskInfo info: all){
            JSONObject json = (JSONObject) JSON.toJSON(info);
            jsonArray.add(json);
        }
        data.put("total",all.size());
        data.put("rows",jsonArray);
        System.out.println(data);
        return data;
    }

    @GetMapping("/test")
    @ResponseBody
    public String test(){
        return "test";
    }

    /**
     * 单文件上传
     *
     * @param file
     * @param name
     * @param cname
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    private ResponseCode upload(@RequestParam("file") MultipartFile file, String name, String cname) throws Exception {
        // 获取文件在服务器上的存储位置
//        String serverPath = globalProperties.getServerPath();
        String serverPath = "D:\\文档\\工作\\SC";
        String newPath = serverPath + "\\" + cname;

        // 获取允许上传的文件扩展名
        String extension = globalProperties.getExtension();

        File filePath = new File(newPath);

        logger.info("文件保存的路径为：" + filePath);
        if (!filePath.exists() && !filePath.isDirectory()) {
            logger.info("目录不存在，则创建目录：" + filePath);
            filePath.mkdir();
        }

        // 判断文件是否为空
        if (file.isEmpty()) {
            return ResponseCode.error("文件为空");
        }
        //判断文件是否为空文件
        if (file.getSize() <= 0) {
            return ResponseCode.error("文件大小为空，上传失败");
        }

        // 判断文件大小不能大于50M
        if (DEFAULT_MAX_SIZE != -1 && file.getSize() > DEFAULT_MAX_SIZE) {
            return ResponseCode.error("上传的文件不能大于50M");
        }

        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取文件扩展名
        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

        // 判断文件扩展名是否正确
        if (!extension.contains(fileExtension)) {
            return ResponseCode.error("文件扩展名不正确");
        }

        SysFileInfo sysFileInfo = new SysFileInfo();
        // 重新生成的文件名
        String saveFileName = System.currentTimeMillis() + fileExtension;
        // 在指定目录下创建该文件
        File targetFile = new File(filePath, saveFileName);

        logger.info("将文件保存到指定目录");
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

        // 保存数据
        sysFileInfo.setFileName(fileName);
        sysFileInfo.setFilePath(serverPath + "/" + saveFileName);
        sysFileInfo.setFileSize(file.getSize());
        sysFileInfo.setTaskName(name);

        logger.info("新增文件数据");
        // 新增文件数据
        sysFileInfoDao.save(sysFileInfo);
        return ResponseCode.success("上传成功");

    }


    /**
     * 下载
     *
     * @param fileId
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/download")
    @ResponseBody
    public ResponseCode downloadFile(@RequestParam("fileId") Integer fileId, HttpServletRequest request, HttpServletResponse response) {
        logger.info("文件ID为：" + fileId);
        // 判断传入参数是否非空
        if (fileId == null) {
            return ResponseCode.error("请求参数不能为空");
        }
        // 根据fileId查询文件表
        Optional<SysFileInfo> sysFileInfo = sysFileInfoDao.findById(fileId);

        Optional<TaskInfo> byId = taskInfoDao.findById(fileId);
        if (sysFileInfo.isPresent()) {
            return ResponseCode.error("下载的文件不存在");
        }
        // 获取文件全路径
        File file = new File(byId.get().getPath());
        String fileNames = byId.get().getName();

        // 判断是否存在磁盘中
        if (file.exists()) {
            response.setContentType("application/octet-stream");
            try {
                response.setHeader("content-disposition", "attachement;filename=" + new String(file.getName().getBytes("utf-8"), "ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

//            // 设置强制下载不打开
//            response.setContentType("application/force-download");
//            // 设置文件名
//            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                return ResponseCode.success("下载成功");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return ResponseCode.error("数据库查询存在，本地磁盘不存在文件");
        }
        return ResponseCode.success("下载失败");
    }


    /**
     * 批量文件上传
     *
     * @param files
     * @return
     * @throws Exception
     */
    @PostMapping("/batchUpload")
    @ResponseBody
    public ResponseCode batchUpload(@RequestParam("files") MultipartFile[] files, String Cname) throws Exception {

        String name = newName(Cname);
        if (files == null) {
            return ResponseCode.error("参数为空");
        }
        for (MultipartFile multipartFile : files) {
            String name1 = multipartFile.getName();
            upload(multipartFile, name1, Cname);
        }
        //新建任务
        //当前时间
        DateTimeFormatter fmTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String format = now.format(fmTime);

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setName(name);
        taskInfo.setTime(format);
        taskInfo.setStatus(0);
        taskInfoDao.save(taskInfo);
        //建完任务直接调用算法接口  公司名字和路径  -》  结果文件路径
        String serverPath = "D:\\文档\\工作\\SC";
        String newPath = serverPath + "\\" + Cname;

        File file = new File(newPath);
//        String func = func(file);
        String func = String.valueOf(getFilePath(file));
        String str1 = func.substring(1);
        String substring = str1.substring(0, func.length() - 2);
        String str = Cname + "#" + substring;

        String get = "http://192.168.3.109:5000/Date";



        System.out.println("开始HTTP0000000000000000000000000");
        Map<String, Object> params1 = new HashMap<String, Object>();

//        params1.put("file_name","大理欧普智能科技有限公司公司公司#/Users/ture/BU/work/专利/大理/大理欧普智能科技有限公司规划表.xls,/Users/ture/BU/work/专利/大理/大理_2021序时账.xls,/Users/ture/BU/work/专利/大理/大理2020序时账.xls,/Users/ture/BU/work/专利/大理/大理_2019序时账.xls");
        params1.put("file_name",str);
        System.out.println("向http 请求发送："+str);

//        String s = HttpTools.get(get, params1);


        //调用HttpThread  url params cname
//        Integer id = taskInfo.getId();
//        HttpThread httpThread = new HttpThread(get,params1,taskInfo,taskInfoDao);
//        Thread thread = new Thread(httpThread);
//        thread.start();



        System.out.println("结束HTTP0000000000000000000000000");
        return ResponseCode.success("批量上传成功");
    }




    /**
     * 批量文件上传
     *
     * @param files
     * @return
     * @throws Exception
     */
    @PostMapping("/patentTow")
    @ResponseBody
    public ResponseCode patentTow(@RequestParam("files") MultipartFile[] files, String Cname) throws Exception {

        String name = newName(Cname);
        if (files == null) {
            return ResponseCode.error("参数为空");
        }
        for (MultipartFile multipartFile : files) {
            String name1 = multipartFile.getName();
            upload(multipartFile, name1, Cname);
        }
        //新建任务
        //当前时间
        DateTimeFormatter fmTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String format = now.format(fmTime);

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setName(name);
        taskInfo.setTime(format);
        taskInfo.setStatus(0);
        taskInfoDao.save(taskInfo);
        //建完任务直接调用算法接口  公司名字和路径  -》  结果文件路径
        String serverPath = "D:\\文档\\工作\\SC";
        String newPath = serverPath + "\\" + Cname;

        File file = new File(newPath);
        String func = String.valueOf(getFilePath(file));
        String str1 = func.substring(1);
        String substring = str1.substring(0, func.length() - 2);
        String str = Cname + "#" + substring;

        String get = "http://192.168.3.109:5000/Date";

        System.out.println("开始HTTP0000000000000000000000000");
        Map<String, Object> params1 = new HashMap<String, Object>();

        params1.put("file_name",str);
        System.out.println("向http 请求发送："+str);

        //调用HttpThread  url params cname
        HttpThread httpThread = new HttpThread(get,params1,taskInfo,taskInfoDao);
        Thread thread = new Thread(httpThread);
        thread.start();

        System.out.println("结束HTTP0000000000000000000000000");
        return ResponseCode.success("批量上传成功");
    }





    public static String newName(String name) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String newName = "任务" + '〔' + localDateTime.getYear() + '〕' + localDateTime.format(DateTimeFormatter.ofPattern("MMdd")) + "-" + name;
        return newName;
    }

    private static String func(File file) {
        File[] fs = file.listFiles();
        String str = "";
        for (File f : fs) {
            String name = f.getName();
            str = name + "," + str;
        }
        return str;
    }

    public void http(String url, String data) {
        String value="12222";
        //创建post请求对象
        HttpPost post = new HttpPost(url);
        try {
            //创建参数集合
            List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
            //添加参数
//            list.add(new BasicNameValuePair("input_data", data));
            list.add(new BasicNameValuePair("name", data));
            list.add(new BasicNameValuePair("age", "1"));
//            list.add(new BasicNameValuePair("releaseDate","2020-07-14 09:55:20"));
            //把参数放入请求对象，，post发送的参数list，指定格式
            post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            //添加请求头参数
            post.addHeader("timestamp","1594695607545");
            CloseableHttpClient client = HttpClients.createDefault();
            //启动执行请求，并获得返回值
            CloseableHttpResponse response = client.execute(post);
            //得到返回的entity对象
            HttpEntity entity = response.getEntity();
            //把实体对象转换为string
            String result = EntityUtils.toString(entity, "UTF-8");
            //返回内容
            System.out.println("result++++++++++++++++++++++++++"+result);
        } catch (Exception e1) {
            e1.printStackTrace();

        }

    }

    public static List<String> getFilePath(File file){
        List filePaths= new ArrayList();
        File[] files = file.listFiles();
        if (files==null||files.length==0){
            return null;
        }
        for (File file1 : files) {
            if (file1.isDirectory()){
                //递归调用
                getFilePath(file1);
            }else {
                //保存文件路径到集合中
                filePaths.add(file1.getAbsolutePath());
            }
        }
        return filePaths;
    }



//    @Async("SyncThread")
//    @RequestMapping(value = "/addBlog")
//    public CompletableFuture<String> addBlog(String url,Map<String, Object> params) {
//        System.out.println("\n\n----------------------------------------------");
//        System.out.println(Thread.currentThread().getName() + "正在处理请求");
//        System.out.println("----------------------------------------------");
//        String result = "请求失败";
//        String s = HttpTools.get(url, params);
//        System.out.println(s);
//        //....你的业务逻辑
//        return CompletableFuture.completedFuture(result);
//    }
//    //这样以后你的这个方法将会交由线程池去进行处理，并将结果返回，一定要记得改返回值类型，否则返回的将是空的。



}



class HttpThread implements Runnable{

    private String url;
    private Map<String, Object> params;
    private TaskInfo taskInfo;
    private TaskInfoDao taskInfoDao;
    public HttpThread(String url, Map<String, Object> params, TaskInfo taskInfo,TaskInfoDao taskInfoDao) {
        this.url = url;
        this.params = params;
        this.taskInfo=taskInfo;
        this.taskInfoDao=taskInfoDao;
    }
    @Override
    public void run() {
        //开始线程
        System.out.println("\n\n----------------------------------------------");
        System.out.println(Thread.currentThread().getName() + "正在处理http请求");
        String result = "请求失败";
        result = HttpTools.get(url, params);
        System.out.println(result);

        String result1 = getResult(result);
        System.out.println("获取结果路径："+result1);
        System.out.println("----------------------------------------------");

//        List<TaskInfo> all = taskInfoDao.findAll();
//        System.out.println(all);
        taskInfo.setPath(result1);
        taskInfo.setStatus(1);
        taskInfoDao.save(taskInfo);

    }

    public String getResult(String results){
        JSONObject parse = (JSONObject) JSONObject.parse(results);
        JSONObject result = (JSONObject) parse.get("result");
        String saved_path = String.valueOf(result.get("saved_path"));
        System.out.println(saved_path);
        return saved_path;
    }
}
