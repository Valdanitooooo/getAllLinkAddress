# getAllLinkAddress

get all link address in chrome developer tools

## why

因为要用jmeter对一个web网页做压力测试, 需要一次请求这个网页中所有的资源才能接近真人操作的效果, 所以使用了[Parallel Controller & Sampler](https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/parallel/Parallel.md)这个插件来合并所有的请求, 但是将所有请求的url复制到这个插件里工作量巨大, 也没找到有什么工具和插件能在开发者工具里一次复制多个请求地址, 所以干脆自己写一个脚本吧, 这个可能不是best practice, 但是个能解决问题的办法。

## use

1. 打开浏览器开发者工具
2. 访问你的请求地址
3. 进入开发者工具Network菜单, 这个菜单下第一排的按钮中有个长得像下载按钮的`Export HAR`按钮, 点击然后保存har格式的文件
4. 用随便什么文本编辑器打开har文件, 可以看到就是一个json, 全选复制
5. 在开发者工具中切换到Console菜单, 输入`var reqs = 粘贴刚才复制的json`, 回车
6. 再输入

```javascript
var urls = ''
reqs.log.entries.forEach(item => {
    urls += item.request.url + "\n"
})
console.log(urls)
```

这个页面中所有的请求就打印出来了

7. 接下来需要把这些url复制到[Parallel Controller & Sampler](https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/parallel/Parallel.md)插件里, 但是这个插件又不支持复制多个, 所以我们打开jmeter的脚本文件, 发现之前配置的url都是这样格式的

```xml
<collectionProp name="urls">
    <collectionProp name="1357392467">
        <stringProp name="726316312">http://xxxxx/xxxxxxxx</stringProp>
    </collectionProp>
    .....
</collectionProp>
```

为了方便复制到jmeter脚本中, 我们再优化一下我们的输出格式, 这个`collectionProp`和`stringProp`的`name`属性也不知道有啥用, 随便填个进去吧

```javascript
var urls = ''
reqs.log.entries.forEach(item => {
    urls += '<collectionProp name="1357392467">\n   <stringProp name="726316312">'+ item.request.url + "</stringProp>\n</collectionProp>\n"
})
console.log(urls)
```

将输出结果复制到jmeter脚本中的`<collectionProp name="urls">`标签中, 保存, 在jmeter中导入脚本, 然后, 报错了。。。

8. 大概看一下报错信息, 应该是格式问题, 对比一下之前配置的url, 看到`&`被转译了, 再改一下脚本

```javascript
var urls = ''
reqs.log.entries.forEach(item => {
    urls += '<collectionProp name="1357392467">\n   <stringProp name="726316312">'+ item.request.url.replace(/&/g,'&amp;') + "</stringProp>\n</collectionProp>\n"
})
console.log(urls)
```

这次可以了, 最终脚本就是这样了

9. 翻车了, har文件太大, 靠浏览器处理不了, 增加一个java版本, 用读写文件的方式, 并使用了jsonpath, 逻辑还是一样。

```java
    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("your har json file path ");
            final String str = IOUtils.toString(fis, "UTF-8");
            List<String> flows = JsonPath.parse(str).read("$.log.entries[*].request.url");
            String urls = "";
            for (String flow : flows) {
                flow = flow.replaceAll("&", "&amp;");
                urls += "<collectionProp name=\"1357392467\">\n   <stringProp name=\"726316312\">"
                        + flow + "</stringProp>\n</collectionProp>\n";
            }
            File file = new File("your output path/urls.xml");
            FileOutputStream outputStream = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(
                    urls.getBytes("utf8"));
            IOUtils.copy(is, outputStream);
            System.out.println("success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```

