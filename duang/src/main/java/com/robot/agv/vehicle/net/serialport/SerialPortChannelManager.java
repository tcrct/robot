package com.robot.agv.vehicle.net.serialport;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.RobotCommAdapter;
import com.robot.agv.vehicle.net.ChannelManagerFactory;
import com.robot.agv.vehicle.net.IChannelManager;
import com.robot.agv.vehicle.net.serialport.rxtx.DataAvailableListener;
import com.robot.agv.vehicle.net.serialport.rxtx.SerialPortManager;
import gnu.io.SerialPort;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 串口方式
 */
public class SerialPortChannelManager implements IChannelManager<Request, Response> {

    private static final Logger LOG = LoggerFactory.getLogger(SerialPortChannelManager.class);

    private ConnectionEventListener<Response> eventListener;
    private TelegramSender sender;
    private SerialPortManager serialPortManager;
    private SerialPort serialPort;
    private boolean initialize;
    private List<String> mCommList = null;

    public SerialPortChannelManager(RobotCommAdapter adapter) {
        this.eventListener = (ConnectionEventListener<Response>)adapter;
        this.sender =(TelegramSender)adapter;
        serialPortManager = new SerialPortManager();
    }

    @Override
    public void initialize() {
        mCommList = serialPortManager.findPorts();

        if(ArrayUtil.isEmpty(mCommList)) {
            throw new NullPointerException("没有找到可用的串串口！");
        }
        initialize = true;
    }

    @Override
    public boolean isInitialized() {
        return initialize;
    }

    @Override
    public void terminate() {
        if (isInitialized() && null != serialPort && isConnected()) {
            serialPortManager.closePort(serialPort);
        }
    }

    @Override
    public void connect(String serialPortName, int baudrate) {
        if (!isInitialized()) {
            throw new RuntimeException("指定的串口初始化不成功");
        }
        if(!mCommList.contains(serialPortName)) {
            throw new IllegalArgumentException("指定的串口名称["+serialPortName+"]与系统允许使用的不符");
        }
        if(baudrate == 0) {
            throw new IllegalArgumentException("串口波特率["+baudrate+"]没有设置");
        }
        try {
            serialPort = serialPortManager.openPort(serialPortName, baudrate);
            //读监听
            readListener();
            LOG.info("串口连接并监听成功，名称[{}]，波特率[{}]", serialPortName, baudrate);
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
        return serialPortManager.isConnected();
    }

    @Override
    public void setLoggingEnabled(boolean enable) {
        LOG.info("该功能未实现");
    }

    @Override
    public void scheduleConnect(@Nonnull String host, int port, long delay) {
        LOG.info("该功能未实现");
    }

    @Override
    public void send(Request telegram) {
        if (ObjectUtil.isEmpty(telegram) && !isConnected()) {
            throw new NullPointerException("待发送的报文不能为空或未连接成功");
        }
        serialPortManager.sendToPort(serialPort, telegram.toString().getBytes());
    }

    public void readListener() {
        if (!isConnected()) {
            LOG.error("串口未连接成功");
             return;
        }
        eventListener.onConnect();
        serialPortManager.addListener(serialPort, new DataAvailableListener() {
            @Override
            public void dataAvailable() {
                String telegramData = serialPortManager.readTelegram(serialPort);
                // 接收到的协议
                ChannelManagerFactory.onIncomingTelegram(eventListener, sender, telegramData);
            }
        });
    }
}
