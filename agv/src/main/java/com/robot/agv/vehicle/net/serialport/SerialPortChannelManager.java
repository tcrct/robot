package com.robot.agv.vehicle.net.serialport;

import cn.hutool.core.util.ArrayUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.IChannelManager;
import com.robot.agv.vehicle.net.serialport.rxtx.SerialPortManager;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 串口方式
 */
public class SerialPortChannelManager implements IChannelManager<Request, Response> {

    private RobotCommAdapter robotCommAdapter;
    private SerialPortManager serialPortManager;
    private SerialPort serialPort;
    private String serialPortName;
    private int baudrate;
    private boolean initialize;

    public SerialPortChannelManager(RobotCommAdapter commAdapter) {
        robotCommAdapter = commAdapter;
        serialPortManager = new SerialPortManager();
    }

    @Override
    public void initialize() {
        List<String> mCommList = serialPortManager.findPorts();

        if(ArrayUtil.isEmpty(mCommList)) {
            throw new NullPointerException("没有找到可用的串串口！");
        }
        if(!mCommList.contains(serialPortName)) {
            throw new IllegalArgumentException("指定的串口名称["+serialPortName+"]与系统允许使用的不符");
        }
        if(baudrate == 0) {
            throw new IllegalArgumentException("串口波特率["+baudrate+"]没有设置");
        }
    }

    @Override
    public boolean isInitialized() {
        return initialize;
    }

    @Override
    public void terminate() {
        if (isInitialized()) {
            serialPortManager.closePort(serialPort);
        }
    }

    @Override
    public void connect(String serialPortName, int baudrate) {
        try {
            serialPort = serialPortManager.openPort(serialPortName, baudrate);
            initialize = true;
        } catch (Exception e) {
            throw new RuntimeException("打开串口时失败，名称["+serialPortName+"]， 波特率["+baudrate+"], 串口可能已被占用！");
        }
    }

    @Override
    public void disconnect() {
        terminate();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void setLoggingEnabled(boolean enable) {

    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {

    }

    @Override
    public void send(Object telegram) {

    }
}
