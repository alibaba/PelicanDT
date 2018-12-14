
package com.alibaba.pelican.deployment.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class FileUtils extends org.apache.commons.io.FileUtils {
    private FileUtils() {

    }

    public static boolean createFile(String res, String filePath) throws IOException {
        boolean flag = true;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            File distFile = new File(filePath);
            File parent = distFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("File '" + distFile + "' could not be created");
            } else {
                bufferedReader = new BufferedReader(new StringReader(res));
                bufferedWriter = new BufferedWriter(new FileWriter(distFile));
                char[] buf = new char[1024];

                int len;
                while ((len = bufferedReader.read(buf)) != -1) {
                    bufferedWriter.write(buf, 0, len);
                }

                bufferedWriter.flush();
                bufferedReader.close();
                bufferedWriter.close();
                return flag;
            }
        } catch (IOException var9) {
            flag = false;
            throw var9;
        }
    }

    public static Collection<File> getCurrentPathFiles(String currentPath) {
        File root = new File(currentPath);
        Collection<File> files = new ArrayList();
        File[] listFiles = root.listFiles();
        File[] arr$ = listFiles;
        int len$ = listFiles.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            File f = arr$[i$];
            if (f.isFile()) {
                files.add(f);
            }
        }

        return files;
    }

    public static Collection<File> getCurrentPathFilesByKeyword(String currentPath, String keyword) {
        Collection<File> selectfiles = new ArrayList();
        Collection<File> files = getCurrentPathFiles(currentPath);
        Iterator filelist = files.iterator();

        while (filelist.hasNext()) {
            File file = (File) filelist.next();
            String fileName = file.getName();
            if (fileName.contains(keyword)) {
                selectfiles.add(file);
            }
        }

        return selectfiles;
    }

    public static Collection<File> getFilesByKeyword(String rootPath, String keyword) {
        File root = new File(rootPath);
        List<File> files = new ArrayList();
        List<File> selectfiles = new ArrayList();
        listFiles(files, root);
        Iterator filelist = files.iterator();

        while (filelist.hasNext()) {
            File file = (File) filelist.next();
            String fileName = file.getName();
            if (fileName.contains(keyword)) {
                selectfiles.add(file);
            }
        }

        return selectfiles;
    }

    private static void listFiles(List<File> files, File dir) {
        File[] listFiles = dir.listFiles();
        if (listFiles != null) {
            File[] arr$ = listFiles;
            int len$ = listFiles.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                File f = arr$[i$];
                if (f.isFile()) {
                    files.add(f);
                } else if (f.isDirectory()) {
                    listFiles(files, f);
                }
            }

        }
    }

    public static String getContentFromFile(String filePath) throws IOException {
        String content = null;

        try {
            if (!(new File(filePath)).exists()) {
                return new String();
            } else {
                content = IOUtils.toString(new FileInputStream(filePath), Charset.defaultCharset());
                return content;
            }
        } catch (IOException var3) {
            throw var3;
        }
    }

    private static long sizeOfDirectory0(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return 0L;
        } else {
            long size = 0L;
            File[] arr$ = files;
            int len$ = files.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                File file = arr$[i$];

                try {
                    if (!isSymlink(file)) {
                        size += sizeOf0(file);
                        if (size < 0L) {
                            break;
                        }
                    }
                } catch (IOException var9) {
                    ;
                }
            }

            return size;
        }
    }

    private static long sizeOf0(File file) {
        return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
    }

    public static boolean isSymlink(File file) throws IOException {
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile()) ? isBrokenSymlink(file) : true;
    }

    private static boolean isBrokenSymlink(File file) throws IOException {
        if (file.exists()) {
            return false;
        } else {
            final File canon = file.getCanonicalFile();
            File parentDir = canon.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                File[] fileInDir = parentDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File aFile) {
                        return aFile.equals(canon);
                    }
                });
                return fileInDir != null && fileInDir.length > 0;
            } else {
                return false;
            }
        }
    }

}
