package com.robot.service.traffic;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.agv.vehicle.telegrams.ProtocolParam;
import com.robot.agv.vehicle.telegrams.StateRequest;
import com.robot.mvc.annotations.Service;
import com.robot.mvc.exceptions.RobotException;
import com.robot.numes.RobotEnum;
import com.robot.service.common.BaseService;
import com.robot.utils.ProtocolUtils;
import com.robot.utils.RobotUtil;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class A001Service extends BaseService {

    private static final Logger LOG =  LoggerFactory.getLogger(A001Service.class);

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
        LOG.info("stateRequest: {}", stateRequest);
        if (null==stateRequest) {
            return "";
        }
        String direction = RobotUtil.DIRECTION_MAP.get(stateRequest.getModel().getName());
        if (ToolsKit.isEmpty(direction)) {
            direction = RobotEnum.FORWARD.getValue();
//            direction = RobotEnum.BACK.getValue();
        }
        List<ProtocolParam> protocolParamList =  getProtocolParamList(stateRequest, response, direction);

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
        LOG.info("车辆[{}]行驶到达[{}]卡号,  {}", request.getProtocol().getDeviceId(), RobotUtil.getReportPoint(request.getProtocol()), ProtocolUtils.converterString(request.getProtocol()));
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
