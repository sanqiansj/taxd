package com.niaobulashi.controller;

import com.alibaba.fastjson.JSONArray;
import net.sf.json.JSONObject;
import com.niaobulashi.Utils.HttpTools;
import com.niaobulashi.common.dto.ResponseCode;
import com.niaobulashi.dao.SysFileInfoDao;
import com.niaobulashi.dao.TaskInfoDao;
import com.niaobulashi.model.SysFileInfo;
import com.niaobulashi.model.TaskInfo;
import com.niaobulashi.properties.GlobalProperties;
import com.niaobulashi.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private PdfService pdfService;

    @GetMapping("/downloadPage")
    public String downloadPage() {
        return "downloadPage";
    }

    @GetMapping("/post-2-j")
    public String postPage() {
        return "post-2-j";
    }

    /**
     * 文件上传页面
     *
     * @return
     */
    @GetMapping("/")
    public String updatePage() {
        return "file";
    }

    /**
     * 展示任务列表
     * @return
     */
    @GetMapping("/showTasks")
    @ResponseBody
    public JSONObject showTask(){
        List<TaskInfo> all = taskInfoDao.findAll(Sort.by("id").descending());

        Map<String, Object> stringJSONArrayHashMap = new HashMap<>();

        JSONObject data= new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (TaskInfo info: all){
//            JSONObject json = (JSONObject) JSON.toJSON(info);
            JSONObject jsonObject = JSONObject.fromObject(info);
            jsonArray.add(jsonObject);
        }
        data.put("total",all.size());
        data.put("rows",jsonArray);
        System.out.println(data);
        return data;
    }

    /**
     * 参数  任务名字
     * 展示 该任务的 文件列表
     *
     * @return
     */
    @GetMapping("/showFileInfo")
    @ResponseBody
    public JSONObject showFileInfo(String taskName){
        String taskName1 = taskName.replace("\"","");
        System.out.println(taskName1);
        List<SysFileInfo> data = sysFileInfoDao.searchByTaskName(taskName1);
        JSONObject data1 = new JSONObject();
        data1.put("total", data.size());
        data1.put("rows", data);
        System.out.println(data1);
        return data1;
    }

    /**
     * 功能二 下载
     * 根据文件id下载  结果文件
     * @return
     */
    @PostMapping("/downloadTow")
    @ResponseBody
    public ResponseCode downloadTow(@RequestParam("fileId") Integer fileId, HttpServletRequest request, HttpServletResponse response){
        logger.info("文件ID为：" + fileId);
        // 判断传入参数是否非空
        if (fileId == null) {
            return ResponseCode.error("请求参数不能为空");
        }
        // 根据fileId查询文件表
        Optional<SysFileInfo> sysFileInfo = sysFileInfoDao.findById(fileId);

        Optional<TaskInfo> byId = taskInfoDao.findById(fileId);
//        if (sysFileInfo.isPresent()) {
//            return ResponseCode.error("下载的文件不存在");
//        }
        // 获取文件全路径
        File file = new File(sysFileInfo.get().getResultPath());
//        String fileNames = byId.get().getName();

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
//                return ResponseCode.success("下载成功");
                return null;
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
    private ResponseCode upload(@RequestParam("input-b6a[]") MultipartFile file, String name, String cname) throws Exception {
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
//        String fileName = name;
//        System.out.println(fileName);
        // 获取文件扩展名
        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

        // 判断文件扩展名是否正确
        if (!extension.contains(fileExtension)) {
            return ResponseCode.error("文件扩展名不正确");
        }

        SysFileInfo sysFileInfo = new SysFileInfo();
        // 重新生成的文件名
        String saveFileName = System.currentTimeMillis() + fileName;
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
        sysFileInfo.setFilePath(serverPath + "\\" + saveFileName);
        sysFileInfo.setFileSize(file.getSize());
        sysFileInfo.setTaskName(cname);
        sysFileInfo.setResultStatus(0);

        logger.info("新增文件数据");
        // 新增文件数据
        sysFileInfoDao.save(sysFileInfo);
        return ResponseCode.success("上传成功");

    }


    /**
     * 下载（功能一）
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
//        Optional<SysFileInfo> sysFileInfo = sysFileInfoDao.findById(fileId);

        Optional<TaskInfo> byId = taskInfoDao.findById(fileId);
//        if (sysFileInfo.isPresent()) {
//            return ResponseCode.error("下载的文件不存在");
//        }
        // 获取文件全路径
        File file = new File(byId.get().getPath());
        String fileNames = byId.get().getName();
        String name = file.getName();
//        try {
//            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
//                name = URLEncoder.encode(name, "UTF-8");
//            } else {
//                name = new String(name.getBytes("UTF-8"), "ISO8859-1");
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        System.out.println("filename+++++++++++++++++++"+fileNames);
        System.out.println("name+++++++++++++++++"+name);

        // 判断是否存在磁盘中
        if (file.exists()) {
//            response.setContentType("application/octet-stream");
            //                response.setContentType("application/"+fileNames.split("\\.")[1]);
//            response.setHeader("Content-Disposition", "attachment; filename="+ name);
            //                response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(name, "UTF-8"));
//            response.setHeader("Content-Disposition", "attachment; filename=" + name);

//            String fileName = attachmentName;
//            response.setContentType("multipart/form-data");
//            //response.setContentType("multipart/form-data;charset=UTF-8");也可以明确的设置一下UTF-8，测试中不设置也可以。
//            response.setHeader("Content-Disposition", "attachment; fileName="+  fileName +";filename*=utf-8''"+URLEncoder.encode(fileName,"UTF-8"));
//





            try {
                response.addHeader("Content-Disposition", "attachment;filename=" + new String(name.getBytes("utf-8"),"ISO8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            // 设置强制下载不打开
//            response.setContentType("application/force-download");
//            // 设置文件名
//            response.addHeader("Content-Disposition", "attachment;fileName=" + name);
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
//                return ResponseCode.success("下载成功");
                return null;
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
     * 功能一
     *
     * @param files
     * @return
     * @throws Exception
     */
    @PostMapping("/batchUpload")
    @ResponseBody
    public ResponseCode batchUpload(@RequestParam("input-b6a[]") MultipartFile[] files, String Cname) throws Exception {
        System.out.println(Cname);
        String name = newName(Cname,1);
        if (files == null) {
            return ResponseCode.error("参数为空");
        }
        for (MultipartFile multipartFile : files) {
            String name1 = multipartFile.getName();
            upload(multipartFile, name1, name);
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
        taskInfo.setType(1);
        taskInfoDao.save(taskInfo);
        //建完任务直接调用算法接口  公司名字和路径  -》  结果文件路径
        String serverPath = "D:\\文档\\工作\\SC";
        String newPath = serverPath + "\\" + name;

        File file = new File(newPath);
//        String func = func(file);





        String func = String.valueOf(getFilePath(file));
        String s = func.replaceAll("(?:\\[|null|\\]| +)", "");
        String str = name + "#" + s;

        String get = "http://0.0.0.0:5000/Date";



        System.out.println("开始HTTP0000000000000000000000000");
        Map<String, Object> params1 = new HashMap<String, Object>();

//        params1.put("file_name","大理欧普智能科技有限公司公司公司#/Users/ture/BU/work/专利/大理/大理欧普智能科技有限公司规划表.xls,/Users/ture/BU/work/专利/大理/大理_2021序时账.xls,/Users/ture/BU/work/专利/大理/大理2020序时账.xls,/Users/ture/BU/work/专利/大理/大理_2019序时账.xls");
        params1.put("file_name",str);
        System.out.println("向http 请求发送："+str);

//        String s = HttpTools.get(get, params1);


        //调用HttpThread  url params cname
        HttpThread httpThread = new HttpThread(get,params1,taskInfo,taskInfoDao);
        Thread thread = new Thread(httpThread);
        thread.start();



        System.out.println("结束HTTP0000000000000000000000000");
        return ResponseCode.success("批量上传成功");
    }




    /**
     * 批量文件上传
     * 功能二
     *
     * @param files   公司名， 一个excel 若干个word 和 pdf
     * @return
     * @throws Exception
     */
    @PostMapping("/patentTow")
    @ResponseBody
    public ResponseCode patentTow(@RequestParam("input-b6a[]") MultipartFile[] files, String Cname) throws Exception {
        String name = newName(Cname,2);
        if (files == null) {
            return ResponseCode.error("参数为空");
        }
        for (MultipartFile multipartFile : files) {
            String name1 = multipartFile.getName();
            upload(multipartFile, name1, name);
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
        taskInfo.setType(2);
        taskInfoDao.save(taskInfo);
        //建完任务直接调用算法接口  公司名字和路径  -》  结果文件路径
        String serverPath = "D:\\文档\\工作\\SC";
        String newPath = serverPath + "\\" + name;

        File file = new File(newPath);
        List<String> filePath = getFilePath(file);
        System.out.println(filePath);

        String pathResult = "";
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject pathjson = new JSONObject();
        JSONObject pathjson1 = new JSONObject();
        JSONObject json = new JSONObject();


//        String saveFileName = "111";


        for (String path:filePath) {
            String fileExtension = path.substring(path.lastIndexOf(".")).toLowerCase();

            String saveFileName = System.currentTimeMillis() + fileExtension;

            String nowPath=path.substring(path.length() -3,path.length());
            if (nowPath.equals("lsx")||nowPath.equals("xls")){
                //获取表路径
                pathResult=path;
                pathjson.put("规划表",path);
                pathjson.put("原始文件路径",serverPath+path);
                jsonObjects.add(pathjson);
            }
            if (nowPath.equals("ocx")||nowPath.equals("doc")){
                //做doc 分析处理 放入jsonarry
                JSONObject data = pdfService.analysisWord(path);
                data.put("原始文件路径",serverPath+path);



                jsonObjects.add(data);
            }
            if (nowPath.equals("pdf")||nowPath.equals("PDF")){
                //做pdf 分析处理 放入jsonarry
                JSONObject data = pdfService.analysispdf(path);
                data.put("原始文件路径",serverPath+path);


                jsonObjects.add(data);
            }
        }
        System.out.println("HTTP发送的数据"+jsonObjects);

        String s = newTxt(jsonObjects.toString());

        String get = "http://192.168.3.109:5001/Date";
//        String get = "http://0.0.0.0:5001/Date";
        System.out.println("开始HTTP0000000000000000000000000");
        Map<String, Object> params1 = new HashMap<String, Object>();
//        params1.put("file_name","/Users/ture/BU/work/专利/3-31/httpParam.txt");
        params1.put("file_name",s);

//        params1.put("file_name","大理欧普智能科技有限公司公司公司#/Users/ture/BU/work/专利/大理/大理欧普智能科技有限公司规划表.xls,/Users/ture/BU/work/专利/大理/大理_2021序时账.xls,/Users/ture/BU/work/专利/大理/大理2020序时账.xls,/Users/ture/BU/work/专利/大理/大理_2019序时账.xls");
        System.out.println("向http 请求发送："+params1);

        HttpThread2 httpThread2 = new HttpThread2(get,params1,sysFileInfoDao,taskInfoDao,taskInfo);
        Thread thread2 = new Thread(httpThread2);
        thread2.start();
        System.out.println("结束HTTP000000000000000000000000");
        return ResponseCode.success("批量上传成功");
    }


    public static String newTxt(String txt){
        String resultPath="";
        try {
            // 保存路径
            String path = "D:\\文档\\工作\\SC";
            String title = System.currentTimeMillis() + "专利解析文件";
            // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw
            /* 写入Txt文件 */
            File mkdirsName = new File(path);// 相对路径，如果没有则要建立一个新的output。txt文件
            if(!mkdirsName.exists()){
                mkdirsName.mkdirs();
            }
            resultPath=path+"\\"+title+".txt";
            File writename = new File(resultPath);// 相对路径，如果没有则要建立一个新的output。txt文件
            // 判断文件是否存在，不存在即新建
            // 存在即根据操作系统添加换行符
            if(!writename.exists()) {
                writename.createNewFile(); // 创建新文件
            } else {
                String osName = System.getProperties().getProperty("os.name");
                if (osName.equals("Linux")) {
                    txt = "\r" + txt;
                } else {
                    txt = "\r\n" + txt;
                }
            }
            // 如果是在原有基础上写入则append属性为true，默认为false
            BufferedWriter out = new BufferedWriter(new FileWriter(writename,true));
            out.write(txt); // 写入TXT
            out.flush(); // 把缓存区内容压入文件
            out.close(); // 最后记得关闭文件
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultPath;
    }






    public static String newName(String name,int type) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String newName="";
        if (type==1){
            newName="辅助账" + '〔' + localDateTime.getYear() + '〕' + localDateTime.format(DateTimeFormatter.ofPattern("MMdd")) + "-" + name;
        }
        if (type==2){
            newName="专利" + '〔' + localDateTime.getYear() + '〕' + localDateTime.format(DateTimeFormatter.ofPattern("MMdd")) + "-" + name;
        }
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

}


/**
 * 功能一 线程
 */
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
//        System.out.println(result);
//
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
        System.out.println(results);
        JSONObject jsonObject = JSONObject.fromObject(results);
        JSONObject result = (JSONObject) jsonObject.get("result");
        String saved_path = String.valueOf(result.get("saved_path"));
        System.out.println(saved_path);
        return saved_path;
    }
}

/**
 *
 * 功能二 线程
 */
class HttpThread2 implements Runnable{

    private String url;
    private Map<String, Object> params;


    private SysFileInfo sysFileInfo;
    private TaskInfoDao taskInfoDao;
    private SysFileInfoDao sysFileInfoDao;
    private TaskInfo taskInfo;
    public HttpThread2(String url, Map<String, Object> params, SysFileInfoDao sysFileInfoDao, TaskInfoDao taskInfoDao, TaskInfo taskInfo) {
        this.url = url;
        this.params = params;
        this.sysFileInfoDao=sysFileInfoDao;
        this.taskInfoDao=taskInfoDao;
        this.taskInfo=taskInfo;
    }
    @Override
    public void run() {
        //开始线程
        System.out.println("\n\n----------------------------------------------");
        System.out.println(Thread.currentThread().getName() + "正在处理http请求");
        String result = "请求失败";
        result = HttpTools.get(url, params);
        System.out.println("获得http结果"+result);
        String result1 = getResult(result,sysFileInfoDao
                ,taskInfoDao,taskInfo);
        System.out.println("获取结果路径："+result1);
        System.out.println("----------------------------------------------");


    }

    public static String getResult(String results, SysFileInfoDao sysFileInfoDao, TaskInfoDao taskInfoDao, TaskInfo taskInfo){
        Object parse = com.alibaba.fastjson.JSONObject.parse(results);
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(parse.toString());
        JSONArray result = (JSONArray) jsonObject.get("result");

        if(result.size()>0){
            for(int i=0;i<result.size();i++){

                JSONObject job = JSONObject.fromObject(result.getJSONObject(i));  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                //结果
                Object saved_path = job.get("saved_path");
//                saved_path = "F:/flask学习资料/Python Flask全程实战-多功能博客系统开发/FlaskDemo-前端页面/lin/3-31/0.docx";
                //原始
                Object result_path = job.get("result_path");
//                result_path = "D:\\文档\\工作\\SC\\1649157003776.pdf";

                SysFileInfo sysFileInfo =sysFileInfoDao.findByFilePath(result_path.toString());
                sysFileInfo.setResultStatus(1);
                sysFileInfo.setResultPath(saved_path.toString());
                SysFileInfo save = sysFileInfoDao.save(sysFileInfo);
                System.out.println("sys--------"+save);
                System.out.println("saved——path————————————   "+saved_path);
                System.out.println("result_path————————————   "+result_path);

//                sysFileInfoDao.findById()

                sysFileInfoDao.updata(saved_path.toString(),result_path.toString());
            }
            taskInfo.setStatus(1);
            TaskInfo save1 = taskInfoDao.save(taskInfo);
            System.out.println("task22222"+save1);
        }
        return null;
    }

    public static void main(String[] args) {
    }

}

