package com.yunxin.mp.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author: huangzuwang
 * @date: 2019-12-02 12:04
 * @description:
 */
@Mojo(name = "generate",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorPlugin extends AbstractMojo {

//    @Parameter
//    private String projectDir;

    @Parameter
    private String configPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String projectDir = System.getProperty("user.dir");
        System.out.println("GeneratorPlugin >>>>>> 读取到项目地址:" + projectDir);
        System.out.println("GeneratorPlugin >>>>>> 读取到配置文件地址:" + configPath);

        System.out.println("GeneratorPlugin >>>>>> 配置文件解析1");
        FileSystemResource fileSystemResource = null;
        try {
            fileSystemResource = new FileSystemResource(configPath);
        }catch (Throwable e){
            System.out.println("GeneratorPlugin >>>>>> 配置文件解析2:" + e);
        }
        System.out.println("GeneratorPlugin >>>>>> 配置文件解析3:" + fileSystemResource);
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(fileSystemResource);
        Properties properties = yaml.getObject();
        System.out.println("GeneratorPlugin >>>>>> 配置文件解析:" + properties);
        Boolean override = new Boolean(properties.getProperty("mp.override"));
        String url = properties.getProperty("mp.datasource.url");
        String driverName = properties.getProperty("mp.datasource.driverName");
        String username = properties.getProperty("mp.datasource.username");
        String password = properties.getProperty("mp.datasource.password");
        String entityPackage = properties.getProperty("mp.entity.package");
        String entityPrefix = properties.getProperty("mp.entity.prefix");
        String entitySuffix = properties.getProperty("mp.entity.suffix");
        String mapperPackage = properties.getProperty("mp.mapper.package");
        Boolean mapperGenerate = new Boolean(properties.getProperty("mp.mapper.generate"));
        Boolean xmlGenerate = new Boolean(properties.getProperty("mp.mapper.xmlGenerate"));
        String xmlPath = properties.getProperty("mp.mapper.xmlPath");
        String mapperPrefix = properties.getProperty("mp.mapper.prefix");
        String mapperSuffix = properties.getProperty("mp.mapper.suffix");
        String tableNames = properties.getProperty("mp.table.names");
        String tablePrefix = properties.getProperty("mp.table.prefix");
        String idType = properties.getProperty("mp.idType");

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setFileOverride(override);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);

        //前后缀
        StringBuffer entityNameBuffer = new StringBuffer();
        if (StringUtils.isNotEmpty(entityPrefix)){
            entityNameBuffer.append(entityPrefix);
        }
        entityNameBuffer.append("%s");
        if (StringUtils.isNotEmpty(entitySuffix)){
            entityNameBuffer.append(entitySuffix);
        }
        gc.setEntityName(entityNameBuffer.toString());
        gc.setOutputDir(projectDir + "/src/main/java");

        StringBuffer mapperNameBuffer = new StringBuffer();
        if (StringUtils.isNotEmpty(mapperPrefix)){
            mapperNameBuffer.append(mapperPrefix);
        }
        mapperNameBuffer.append("%s");
        if (StringUtils.isNotEmpty(mapperSuffix)){
            mapperNameBuffer.append(mapperSuffix);
        }
        mapperNameBuffer.append("Mapper");
        gc.setMapperName(mapperNameBuffer.toString());
        gc.setXmlName(gc.getMapperName());
//        gc.setAuthor("jobob");
        gc.setOpen(false);
        //主键策略
        switch (idType){
            case "auto":
                gc.setIdType(IdType.AUTO);
                break;
            case "input":
                gc.setIdType(IdType.INPUT);
                break;
            case "worker":
                gc.setIdType(IdType.ID_WORKER);
                break;
            case "worker_str":
                gc.setIdType(IdType.ID_WORKER_STR);
                break;
            case "uuid":
                gc.setIdType(IdType.UUID);
                break;
                default:
                gc.setIdType(IdType.NONE);
                break;
        }

        // gc.setSwagger2(true); 实体属性 Swagger2 注解
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        dsc.setDriverName(driverName);
        dsc.setUsername(username);
        dsc.setPassword(password);

        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
//        pc.setModuleName(scanner("模块名"));
        pc.setParent("");
        pc.setEntity(entityPackage);
        pc.setMapper(mapperPackage);
        mpg.setPackageInfo(pc);


        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        // 如果模板引擎是 freemarker
//        String templatePath = "/templates/mapper.xml.ftl";
        // 如果模板引擎是 velocity
//         String templatePath = "/templates/mapper.xml.vm";

        String entityTemplate = "/META-INF/templates/entity.java.vm";
        String mapperTemplate = "/META-INF/templates/mapper.java.vm";
        String mapperXmlTemplate = "/META-INF/templates/mapper.xml.vm";
//         自定义输出配置
        // 只生成entity和mapper

        List<FileOutConfig> focList = new ArrayList<>();
//         自定义配置会被优先输出
        focList.add(new FileOutConfig(entityTemplate) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出目录
                String packagePath = pc.getEntity().replace(".", "/") + "/";
                String path = projectDir + "/src/main/java/" + packagePath + tableInfo.getEntityName() + ".java";
                return path;
            }
        });

        if (mapperGenerate){
            focList.add(new FileOutConfig(mapperTemplate) {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    //自定义输出目录
                    String packagePath = pc.getMapper().replace(".", "/") + "/";
                    String path = String.format("%s/src/main/java/%s%s.java", projectDir, packagePath, tableInfo.getMapperName());
                    System.out.println(">>>>>>mapper生成目录" + path);
                    return path;
                }
            });
        }

        cfg.setFileOutConfigList(focList);

        if (xmlGenerate){
            focList.add(new FileOutConfig(mapperXmlTemplate) {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    //自定义输出目录
                    String path = String.format("%s/src/main/%s/%s.xml", projectDir, xmlPath, tableInfo.getXmlName());
                    System.out.println(">>>>>>xml生成目录" + path);
//                    String path = projectDir + "/src/main/" + xmlPath + tableInfo.getXmlName() + ".xml";
                    return path;
                }
            });
        }

        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 配置自定义输出模板
        //指定自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
        templateConfig.setEntity(null);
        templateConfig.setMapper(null);
        templateConfig.setService(null);
        templateConfig.setServiceImpl(null);
        templateConfig.setController(null);
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        if (StringUtils.isNotEmpty(tablePrefix)){
            strategy.setTablePrefix(tablePrefix);
        }
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        // 写于父类中的公共字段
        strategy.setInclude(tableNames.split(","));
        mpg.setStrategy(strategy);
//        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }
}
