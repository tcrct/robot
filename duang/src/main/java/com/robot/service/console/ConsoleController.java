package com.robot.service.console;


import com.google.inject.Inject;
import com.robot.core.AppContext;
import com.robot.mvc.annotations.Controller;
import com.robot.mvc.annotations.Mapping;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.opentcs.components.kernel.services.VehicleService;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

/**
 * 将请求到spark服务器的请求转向到该类进行处理
 * 流程步骤如下：
 * 1，请求到达spark后，在 org.opentcs.kernel.extensions.servicewebapi.console.ConsoleRequestHandler 类将请求转到该类
 * 2，ConsoleController类 Method 的 Mapping注解值，必须要与请求到spark的 URI  一致，如有多个注解必须写在第一位，否则会导致请求失败
 * 3，再通过反射调用注入的Service类，处理后将结果返回，最后通过spark返回到客户端
 *
 * 如更改了该Controller的路径，须在ConsoleRequestHandler里同步更改 CONTROLLER_CLASS_NAME 常量值
 *
 */

@Controller
public class ConsoleController {

//    @Inject
//    private ConsoleService consoleService;
//
//    /**
//     * 设置位置
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/position")
//    public String setPosition(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String position = valueIfKeyPresent(request.queryMap(), "position");
//        return consoleService.setPosition(vehicleName, position, getVehicleService(request));
//    }
//
//    /**
//     * 能量等级
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/energylevel")
//    public String setEnergyLevel(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String level = valueIfKeyPresent(request.queryMap(), "level");
//
//        return consoleService.setEnergyLevel(vehicleName, Integer.parseInt(level), getVehicleService(request));
//    }
//
//    /**
//     * 车辆暂停
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/vehiclepaused")
//    public String setVehiclePaused(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String isPaused = valueIfKeyPresent(request.queryMap(), "isPaused");
//
//        return consoleService.setVehiclePaused(vehicleName, Boolean.parseBoolean(isPaused), getVehicleService(request));
//    }
//
//    /**
//     * 最大前进速度
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/maxfwdvelotxt")
//    public String setMaxFwdVeloTxt(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String max = valueIfKeyPresent(request.queryMap(), "max");
//
//        return consoleService.setMaxFwdVeloTxt(vehicleName, Integer.parseInt(max), getVehicleService(request));
//    }
//
//    /**
//     * 设置方向
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/orientation")
//    public String setOrientationAngle(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String angle = valueIfKeyPresent(request.queryMap(), "angle");
//        try {
//            double angleDouble = Double.parseDouble(angle);
//            return consoleService.setOrientationAngle(vehicleName, angleDouble, getVehicleService(request));
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//
//    /**
//     * 设置状态
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/state")
//    public String setState(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String state = valueIfKeyPresent(request.queryMap(), "state");
//
//        return consoleService.setState(vehicleName, state, getVehicleService(request));
//    }
//
//    /**
//     * 重发协议指令
//     * @param request  spark请求对象
//     * @param response spark返回对象
//     * @return
//     */
//    @Mapping(value = "/console/continuevehiclemove")
//    public String continueVehicleMove(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//
//        return consoleService.continueVehicleMoveCommand(vehicleName,  getVehicleService(request));
//    }
//
//    /**
//     * 手动自动模式
//     * isEnabled为false时为自动模式
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/setsinglestepmode")
//    public String setSingleStepModeEnabled(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String enabled = valueIfKeyPresent(request.queryMap(), "isEnabled");
//
//        return consoleService.setSingleStepModeEnabled(vehicleName,  Boolean.parseBoolean(enabled), getVehicleService(request));
//    }
//
//    /**
//     * 手动模式下的下一步按钮
//     *
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/nextstepbutton")
//    public String nextStepButton(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//
//        return consoleService.nextStepButton(vehicleName,  getVehicleService(request));
//    }
//
//    /**
//     * 车辆暂停
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/paused")
//    public String vehiclePaused(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String paused = valueIfKeyPresent(request.queryMap(), "paused");
//
//        return consoleService.vehiclePaused(vehicleName,  Boolean.parseBoolean(paused), getVehicleService(request));
//    }
//
//    /**
//     * 开启适配器
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/enableadapter")
//    public String enableAdapter(Request request, Response response) {
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        String isEnable = valueIfKeyPresent(request.queryMap(), "isEnable");
//
//        return consoleService.enableAdapter(vehicleName,  Boolean.parseBoolean(isEnable), getVehicleService(request));
//    }
//
//    /**
//     * 取车辆信息列表
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/getvehiclelist")
//    public String getVehicleList(Request request, Response response) {
//        response.header(HttpHeaderNames.CONTENT_TYPE.toString(), "text/xml;charset=UTF-8");
//        return consoleService.getVehicleList(getVehicleService(request));
//    }
//
//    /**
//     * 根据车辆名称取车辆信息
//     * @param request
//     * @param response
//     * @return
//     */
//    @Mapping(value = "/console/getvehicle")
//    public String getVehicle(Request request, Response response) {
//        response.header(HttpHeaderNames.CONTENT_TYPE.toString(), "text/xml;charset=UTF-8");
//        String vehicleName = valueIfKeyPresent(request.queryMap(), "vehicleName");
//        return consoleService.getVehicle(getVehicleService(request), vehicleName);
//    }
//
//
//
//    private VehicleService getVehicleService(Request request) {
//        VehicleService vehicleService = request.attribute("AGV_VehicleService");
//        if(null == vehicleService) {
//            vehicleService = AppContext.getKernelServicePortal().getVehicleService();
//        }
//        return vehicleService;
//    }
//
//    private String valueIfKeyPresent(QueryParamsMap queryParams, String key) {
//        if (queryParams.hasKey(key)) {
//            return queryParams.value(key);
//        }
//        else {
//            return null;
//        }
//    }

}
