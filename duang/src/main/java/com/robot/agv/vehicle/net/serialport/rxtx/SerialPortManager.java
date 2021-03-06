package com.robot.agv.vehicle.net.serialport.rxtx;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import gnu.io.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * 串口管理
 *
 * @author Laotang
 */
public class SerialPortManager {

    private static final Log logger = LogFactory.get();
    private static boolean isConnected;

    public SerialPortManager() {

    }
    /**
     * 查找所有可用端口
     *
     * @return 可用端口名称列表
     */
    public final ArrayList<String> findPorts() {
        // 获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> portNameList = new ArrayList<String>();
        // 将可用串口名添加到List并返回该List
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
        }
        return portNameList;
    }

    /**
     * 打开串口
     *
     * @param portName
     *            端口名称
     * @param baudrate
     *            波特率
     * @return 串口对象
     * @throws PortInUseException
     *             串口已被占用
     */
    public final SerialPort openPort(String portName, int baudrate) throws PortInUseException {
        try {
            // 通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            // 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, 2000);
            // 判断是不是串口
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                try {
                    // 设置一下串口的波特率等参数
                    // 数据位：8
                    // 停止位：1
                    // 校验位：None
                    serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    isConnected = true;
                } catch (UnsupportedCommOperationException e) {
                    logger.error(e.getMessage(), e);
                }
                return serialPort;
            }
        } catch (NoSuchPortException e1) {
            logger.error(e1.getMessage(), e1);
        }
        return null;
    }

    /**
     * 关闭串口
     *
     * @param serialPort
     *            待关闭的串口对象
     */
    public void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
            isConnected = false;
        }
    }

    /**
     * 往串口发送数据
     *
     * @param serialPort
     *            串口对象
     * @param order
     *            待发送数据
     */
    public void sendToPort(SerialPort serialPort, byte[] order) {
        if (!isConnected) return;
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 从串口读取数据
     *
     * @param serialPort
     *            当前已建立连接的SerialPort对象
     * @return 读取到的数据
     */
    private byte[] readFromPort(SerialPort serialPort) {
        InputStream in = null;
        byte[] bytes = {};
        try {
            in = serialPort.getInputStream();
            // 缓冲区大小为一个字节
            byte[] readBuffer = new byte[1];
            int bytesNum = in.read(readBuffer);
            while (bytesNum > 0) {
                bytes = concat(bytes, readBuffer);
                bytesNum = in.read(readBuffer);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return bytes;
    }

    /**
     * 添加监听器
     *
     * @param serialPort
     *            串口对象
     * @param listener
     *            串口存在有效数据监听
     */
    public void addListener(SerialPort serialPort, DataAvailableListener listener) {
        try {
            // 给串口添加监听器
            serialPort.addEventListener(new SerialPortListener(listener));
            // 设置当有数据到达时唤醒监听接收线程
            serialPort.notifyOnDataAvailable(true);
            // 设置当通信中断时唤醒中断线程
            serialPort.notifyOnBreakInterrupt(true);
        } catch (TooManyListenersException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 合并数组
     *
     * @param firstArray
     *            第一个数组
     * @param secondArray
     *            第二个数组
     * @return 合并后的数组
     */
    private byte[] concat(byte[] firstArray, byte[] secondArray) {
        if (firstArray == null || secondArray == null) {
            return null;
        }
        byte[] bytes = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, bytes, 0, firstArray.length);
        System.arraycopy(secondArray, 0, bytes, firstArray.length, secondArray.length);
        return bytes;
    }

    /**
     * 读取报文消息
     * @param serialPort
     * @return
     */
    public String readTelegram(SerialPort serialPort) {
        java.util.Objects.requireNonNull(serialPort, "串口对象不能为null");
        byte[] data = null;
        try {
            // 读取串口数据
            data = readFromPort(serialPort);
            // 以字符串的形式接收数据
            return new String(data, CharsetUtil.UTF_8);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }
}
