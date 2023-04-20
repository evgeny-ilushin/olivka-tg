package irc.tula.tg.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ExecCommand {
    private Semaphore outputSem;

    public String output = null;
    private Semaphore errorSem;
    private String error;
    private Process p;

    private class OutputReader extends Thread {
        public OutputReader() {
            try {
                outputSem = new Semaphore(1);
                outputSem.acquireUninterruptibly();
            } finally {
                outputSem.release();
            }
        }

        public void run() {
            try {
                StringBuffer readBuffer = new StringBuffer();
                BufferedReader isr = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String buff = new String();
                while ((buff = isr.readLine()) != null) {
                    readBuffer.append(buff);
                    //System.out.println(buff);
                }
                output = readBuffer.toString();
                outputSem.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ErrorReader extends Thread {
        public ErrorReader() {
            try {
                outputSem = new Semaphore(1);
                outputSem.acquireUninterruptibly();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                outputSem.release();
            }
        }

        public void run() {
            try {
                StringBuffer readBuffer = new StringBuffer();
                BufferedReader isr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String buff = new String();
                while ((buff = isr.readLine()) != null) {
                    readBuffer.append(buff);
                    System.err.println(buff);
                }
                output = readBuffer.toString();
                outputSem.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ExecCommand(String command) {
        try {
            p = Runtime.getRuntime().exec(makeArray(command));
            new OutputReader().start();
            new ErrorReader().start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ExecCommand(String[] commands) {
        try {
            p = Runtime.getRuntime().exec(commands);
            new OutputReader().start();
            new ErrorReader().start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getOutput() {
        try {
            outputSem.acquireUninterruptibly();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputSem.release();
        }
        String value = output;
        outputSem.release();
        return value;
    }

    private String[] makeArray(String command) {
        ArrayList<String> commandArray = new ArrayList<String>();
        String buff = "";
        boolean lookForEnd = false;
        for (int i = 0; i < command.length(); i++) {
            if (lookForEnd) {
                if (command.charAt(i) == '\"') {
                    if (buff.length() > 0) commandArray.add(buff);
                    buff = "";
                    lookForEnd = false;
                } else {
                    buff += command.charAt(i);
                }
            } else {
                if (command.charAt(i) == '\"') {
                    lookForEnd = true;
                } else if (command.charAt(i) == ' ') {
                    if (buff.length() > 0) commandArray.add(buff);
                    buff = "";
                } else {
                    buff += command.charAt(i);
                }
            }
        }
        if (buff.length() > 0) commandArray.add(buff);

        String[] array = new String[commandArray.size()];
        for (int i = 0; i < commandArray.size(); i++) {
            array[i] = commandArray.get(i);
        }

        return array;
    }

    public static void main(String[] args) {
        ExecCommand ec = new ExecCommand("/projects/olivka/telegram-2018/test.sh");
        String res = ec.getOutput();
        System.out.println(ec.output);

    }
}
