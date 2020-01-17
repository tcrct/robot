package com.robot.service.injectionmolding;


import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.mvc.annotations.Service;
import com.robot.service.common.BaseService;
import com.robot.utils.RobotUtil;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/***
 * 注塑机
 */
@Service
public class A006Service extends BaseService {

    private static final Logger LOG =  LoggerFactory.getLogger(A006Service.class);


    /**
     * setrout指令处理
     * @param request 请求对象
     * @param response 响应对象
     * @return
     */
    public String setRout(Request request, Response response) {
        return super.setRout(request, response);
    }

    public String rptAc(Request request, Request response) {
        LOG.info("车辆[{}]行驶到达[{}]卡号", request.getProtocol().getDeviceId(), RobotUtil.getReportPoint(request.getProtocol()));
        return request.getRawContent();
    }

    public String rptRtp(Request request, Request response) throws Exception {
        LOG.info("车辆[{}]接收到预停车到位协议：[{}]", request.getProtocol().getDeviceId(), request.getRawContent());
        return request.getRawContent();
    }

}
