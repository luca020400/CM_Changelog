package org.cyanogenmod.changelog;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * Copyright (c) 2016.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Cmd {
    private static String TAG = "Cmd";

    public static String exec(String... strings) {
        String output = "";
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            Process process = Runtime.getRuntime().exec("sh");
            outputStream = new DataOutputStream(process.getOutputStream());
            inputStream = process.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s + "\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Log.e(TAG, "", e);
            }

            output = readFully(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            CloseSilently(outputStream, inputStream);
        }

        return output;
    }

    private static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }

        return byteArrayOutputStream.toString("UTF-8");
    }

    private static void CloseSilently(Object... objects) {
        for (Object object : objects) {
            if (object != null) {
                try {
                    Log.d(TAG, "Closing: " + String.valueOf(object));

                    if (object instanceof Closeable) {
                        ((Closeable) object).close();
                        return;
                    }

                    Log.d(TAG, "cannot close: " + String.valueOf(object));
                    throw new RuntimeException("cannot close " + object);
                } catch (Throwable e) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }
}