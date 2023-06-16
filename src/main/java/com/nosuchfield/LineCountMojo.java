package com.nosuchfield;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mojo(name = "count")
public class LineCountMojo extends AbstractMojo {

    /**
     * 源码目录
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}", readonly = true, required = true)
    private File sourceDirectory;

    /**
     * 资源信息
     */
    @Parameter(defaultValue = "${project.build.resources}", readonly = true, required = true)
    private List<Resource> resources;

    /**
     * 需要统计的文件后缀
     */
    @Parameter(name = "postfixes")
    private final String[] postfixes = new String[]{"java"};

    private final Map<String, Integer> fileMap = new HashMap<>();

    private final Map<String, Integer> lineMap = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info(sourceDirectory.getAbsolutePath());
        List<File> directories = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resources)) {
            directories = resources.stream().map(FileSet::getDirectory).collect(Collectors.toSet())
                    .stream().map(File::new).collect(Collectors.toList());
        }
        directories.forEach(directory -> {
            getLog().info("根目录：" + directory.getAbsolutePath());
            listDirectories(directory);
        });
        getLog().info("需要统计的文件后缀：" + Arrays.toString(postfixes));
        getLog().info("------------------------------ 文件数量 ------------------------------");
        fileMap.forEach((k, v) -> {
            getLog().info(k + ": " + v);
        });
        getLog().info("-------------------------------- 行数 --------------------------------");
        lineMap.forEach((k, v) -> {
            getLog().info(k + ": " + v);
        });
    }

    /**
     * 递归的获取目录列表
     */
    private void listDirectories(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                Arrays.stream(files).forEach(this::listDirectories);
            }
        }
        if (file.isFile()) {
            countFile(file);
        }
    }

    /**
     * 检查文件
     */
    private void countFile(File file) {
        String name = file.getName();
        for (String postfix : postfixes) {
            if (name.endsWith(postfix)) {
                if (!fileMap.containsKey(postfix)) {
                    fileMap.put(postfix, 0);
                }
                fileMap.put(postfix, fileMap.get(postfix) + 1);

                if (!lineMap.containsKey(postfix)) {
                    lineMap.put(postfix, 0);
                }
                lineMap.put(postfix, lineMap.get(postfix) + getFileLinesCount(file));
                break;
            }
        }
    }

    private int getFileLinesCount(File file) {
        try {
            try (Stream<String> lines = Files.lines(file.toPath())) {
                return (int) lines.count();
            }
        } catch (IOException e) {
            getLog().warn(e.getMessage());
            return 0;
        }
    }

}
