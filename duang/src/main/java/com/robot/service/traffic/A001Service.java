package com.robot.service.traffic;

import cn.hutool.core.util.StrUtil;
import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.common.telegrams.TelegramSender;
import com.robot.agv.vehicle.telegrams.Protocol;
import com.robot.agv.vehicle.telegrams.ProtocolParam;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.core.AppContext;
import com.robot.mvc.annotations.Service;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
import com.robot.service.common.BaseService;
import com.robot.service.common.requests.get.GetAcRequest;
import com.robot.service.common.requests.get.GetMtRequest;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class A001Service extends BaseService {

    private static final Logger LOG =  LoggerFactory.getLogger(A001Service.class);
    private static String DIRECTION = "";  //方向



    /**
     * 下发车辆移动指令
     * @param request
     * @param response
     * @return
     */
    public String setRout(Request request, Response response) {
        if (!(request instanceof StateRequest)) {
            throw new RobotException("该请求不是移动命令请求");
        }
        StateRequest stateRequest =(StateRequest)request;
        if (ToolsKit.isEmpty(DIRECTION)) {
            DIRECTION = RobotEnum.FORWARD.getValue();
        }
        List<ProtocolParam> protocolParamList =  getProtocolParamList(stateRequest, response, DIRECTION);

        if (protocolParamList.isEmpty()) {
            throw new RobotException("协议参数列表对象不能为空");
        }
        return getProtocolString(stateRequest, protocolParamList);
    }

    /**
     * 上报卡号
     * @param request
     * @param response
     * @return
     */
    public String rptAc(Request request, Response response) {
        LOG.info("车辆[{}]行驶到达[{}]卡号", request.getProtocol().getDeviceId(), RobotUtil.getReportPoint(request.getProtocol()));
        Protocol protocol = request.getProtocol();
        String params = protocol.getParams();
        String[] paramsArray = StrUtil.split(params, RobotEnum.SEPARATOR.getValue());

        if ("218".equals(paramsArray[0])) {
            DIRECTION = paramsArray[1];
            LOG.info("当前点为{}， 方向为{}", paramsArray[0], DIRECTION);
        }

        if ("223".equals(paramsArray[0])) {
            if(RobotEnum.BACK.getValue().equals(paramsArray[1])) {
                DIRECTION = RobotEnum.FORWARD.getValue();
            }
            if(RobotEnum.FORWARD.getValue().equals(paramsArray[1])) {
                DIRECTION = RobotEnum.BACK.getValue();
            }
            LOG.info("当前点为{}， 方向为{}", paramsArray[0], DIRECTION);
        }
        return request.getRawContent();
    }

    /**
     * 预停车到位
     * @param request
     * @param response
     * @return
     */
    public String rptRtp(Request request, Response response) {
        LOG.info("车辆[{}]接收到预停车到位协议：[{}]", request.getProtocol().getDeviceId(), request.getRawContent());
        return request.getRawContent();
    }

}
