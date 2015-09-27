package com.luca020400.cmchangelog.misc;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Cmd {
    public static String exec(String... strings) {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;

        try {
            Process su = Runtime.getRuntime().exec("sh");
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s + "\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = readFully(response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseSilently(outputStream, response);
        }

        return res;
    }

    private static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return baos.toString("UTF-8");
    }

    private static void CloseSilently(Object... xs) {
        for (Object x : xs) {
            if (x != null) {
                try {
                    Log.d("Closing: ", String.valueOf(x));

                    if (x instanceof Closeable) {
                        ((Closeable) x).close();
                    } else {
                        Log.d("cannot close: ", String.valueOf(x));
                        throw new RuntimeException("cannot close " + x);
                    }
                } catch (Throwable e) {
                    Log.e("Error :", String.valueOf(e));
                }
            }
        }
    }
}