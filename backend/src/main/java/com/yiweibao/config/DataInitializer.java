package com.yiweibao.config;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.yiweibao.entity.DiagnosisRule;
import com.yiweibao.entity.Equipment;
import com.yiweibao.entity.User;
import com.yiweibao.entity.WorkOrder;
import com.yiweibao.repository.DiagnosisRuleRepository;
import com.yiweibao.repository.EquipmentRepository;
import com.yiweibao.repository.UserRepository;
import com.yiweibao.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final DiagnosisRuleRepository diagnosisRuleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String uploadPath;

    public DataInitializer(UserRepository userRepository, EquipmentRepository equipmentRepository,
                           WorkOrderRepository workOrderRepository,
                           DiagnosisRuleRepository diagnosisRuleRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.upload.path}") String uploadPath) {
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
        this.workOrderRepository = workOrderRepository;
        this.diagnosisRuleRepository = diagnosisRuleRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadPath = uploadPath;
    }

    @Override
    public void run(String... args) {
        backfillRatedParams();
        if (diagnosisRuleRepository.count() == 0) seedDiagnosisRules();
        if (userRepository.count() > 0) return;

        User admin = createUser("admin", "123456", "张管理", 2, "13800000001");
        User engineer1 = createUser("engineer1", "123456", "李维修", 1, "13800000002");
        User engineer2 = createUser("engineer2", "123456", "王师傅", 1, "13800000003");
        User operator1 = createUser("operator1", "123456", "赵操作", 0, "13800000004");

        Equipment e1 = createEquipment("EQ-001", "数控车床 CK6150", "CK6150", "Φ500×1500mm", "沈阳机床", "机加车间-A区", "机加车间", "张管理",
                LocalDate.of(2020, 3, 15), LocalDate.of(2020, 4, 1), 0,
                1400.0, 7.5, 16.0, 70.0);
        Equipment e2 = createEquipment("EQ-002", "立式加工中心 VMC850", "VMC850", "850×500×500mm", "大连机床", "机加车间-A区", "机加车间", "张管理",
                LocalDate.of(2019, 6, 20), LocalDate.of(2019, 7, 1), 0,
                8000.0, 11.0, 24.0, 75.0);
        Equipment e3 = createEquipment("EQ-003", "数控铣床 XK7132", "XK7132", "320×1000mm", "云南CY", "机加车间-B区", "机加车间", "张管理",
                LocalDate.of(2021, 1, 10), LocalDate.of(2021, 2, 1), 1,
                6000.0, 5.5, 12.0, 70.0);
        Equipment e4 = createEquipment("EQ-004", "精密磨床 M1432", "M1432", "Φ320×1000mm", "上海机床", "机加车间-B区", "机加车间", "张管理",
                LocalDate.of(2018, 9, 5), LocalDate.of(2018, 10, 1), 0,
                3000.0, 5.5, 12.0, 65.0);
        Equipment e5 = createEquipment("EQ-005", "数控冲床 HP300", "HP300", "300kN", "济南二机", "冲压车间", "冲压车间", "赵操作",
                LocalDate.of(2022, 5, 12), LocalDate.of(2022, 6, 1), 2,
                500.0, 7.5, 16.0, 70.0);
        Equipment e6 = createEquipment("EQ-006", "卧式镗床 TP619", "TP619", "Φ90mm", "武汉重型", "机加车间-A区", "机加车间", "张管理",
                LocalDate.of(2017, 3, 20), LocalDate.of(2017, 4, 1), 0,
                2000.0, 11.0, 24.0, 70.0);
        Equipment e7 = createEquipment("EQ-007", "龙门铣床 X2020", "X2020", "2000×6000mm", "济南二机", "机加车间-C区", "机加车间", "张管理",
                LocalDate.of(2018, 11, 8), LocalDate.of(2019, 1, 1), 0,
                6000.0, 15.0, 32.0, 75.0);
        Equipment e8 = createEquipment("EQ-008", "钻铣加工中心 T600", "T600", "600×400mm", "大连机床", "装配车间", "装配车间", "李维修",
                LocalDate.of(2023, 2, 15), LocalDate.of(2023, 3, 1), 0,
                8000.0, 7.5, 16.0, 70.0);
        Equipment e9 = createEquipment("EQ-009", "数控滚齿机 Y3150", "Y3150", "Φ500×M8", "重庆机床", "机加车间-B区", "机加车间", "张管理",
                LocalDate.of(2020, 7, 22), LocalDate.of(2020, 8, 1), 0,
                2000.0, 5.5, 12.0, 70.0);
        Equipment e10 = createEquipment("EQ-010", "剪板机 QC12Y", "QC12Y", "6×3200mm", "天水锻压", "冲压车间", "冲压车间", "赵操作",
                LocalDate.of(2021, 9, 10), LocalDate.of(2021, 10, 1), 0,
                0.0, 7.5, 16.0, 60.0);
        Equipment e11 = createEquipment("EQ-011", "电火花成型机 D7140", "D7140", "400×300mm", "苏州三光", "机加车间-C区", "机加车间", "张管理",
                LocalDate.of(2022, 4, 5), LocalDate.of(2022, 5, 1), 0,
                0.0, 4.0, 9.0, 65.0);
        Equipment e12 = createEquipment("EQ-012", "数控折弯机 WC67Y", "WC67Y", "100/3200", "天水锻压", "冲压车间", "冲压车间", "赵操作",
                LocalDate.of(2020, 12, 18), LocalDate.of(2021, 1, 1), 1,
                0.0, 7.5, 16.0, 65.0);
        Equipment e13 = createEquipment("EQ-013", "卧式车床 CW6180", "CW6180", "Φ800×3000mm", "沈阳机床", "机加车间-A区", "机加车间", "张管理",
                LocalDate.of(2016, 5, 30), LocalDate.of(2016, 6, 15), 0,
                800.0, 11.0, 24.0, 75.0);
        Equipment e14 = createEquipment("EQ-014", "立式钻床 Z5140", "Z5140", "Φ40mm", "杭州机床", "装配车间", "装配车间", "李维修",
                LocalDate.of(2019, 8, 15), LocalDate.of(2019, 9, 1), 0,
                3000.0, 4.0, 9.0, 65.0);
        Equipment e15 = createEquipment("EQ-015", "万能工具磨床 M6025", "M6025", "Φ250mm", "武汉机床", "机加车间-C区", "机加车间", "张管理",
                LocalDate.of(2021, 6, 25), LocalDate.of(2021, 7, 15), 0,
                4000.0, 3.0, 7.0, 65.0);
        Equipment e16 = createEquipment("EQ-016", "火焰切割机 GZ4000", "GZ4000", "4000×12000mm", "上海通用", "冲压车间", "冲压车间", "赵操作",
                LocalDate.of(2023, 1, 10), LocalDate.of(2023, 2, 1), 0,
                0.0, 5.5, 12.0, 60.0);
        Equipment e17 = createEquipment("EQ-017", "卧式加工中心 HMC630", "HMC630", "630×630mm", "宁夏小巨人", "机加车间-B区", "机加车间", "张管理",
                LocalDate.of(2022, 10, 8), LocalDate.of(2022, 11, 1), 0,
                6000.0, 15.0, 32.0, 75.0);
        Equipment e18 = createEquipment("EQ-018", "液压机 YT32-315", "YT32-315", "3150kN", "合肥锻压", "冲压车间", "冲压车间", "赵操作",
                LocalDate.of(2019, 12, 1), LocalDate.of(2020, 1, 1), 3,
                0.0, 15.0, 32.0, 70.0);

        createWorkOrder(e3, "赵操作", "主轴异响严重，加工精度不达标", "机械故障", 1, 1, "李维修",
                "主轴轴承磨损导致径向跳动超差", "更换主轴前轴承组，重新校准精度", "SKF 7014AC 角接触球轴承×2", 0);
        createWorkOrder(e5, "赵操作", "液压系统压力不稳定，冲压无力", "液压故障", 2, 0, null, null, null, null, 0);
        createWorkOrder(e12, "赵操作", "折弯角度偏移，重复定位精度差", "机械故障", 1, 0, null, null, null, null, 0);
        createWorkOrder(e1, "张管理", "冷却液管路堵塞，主轴温升异常", "温控故障", 0, 2, "王师傅",
                "冷却液过滤器堵塞导致流量不足", "清洗冷却液过滤器，更换冷却液，清理管路", "冷却液过滤器滤芯×1", 2);
        createWorkOrder(e4, "张管理", "磨削表面出现振纹，粗糙度超标", "机械故障", 1, 2, "李维修",
                "砂轮不平衡导致振动，需重新动平衡", "重新进行砂轮动平衡校正，修整砂轮表面", null, 2);
        createWorkOrder(e7, "张管理", "工作台运行有异响，Y轴运动不顺畅", "机械故障", 1, 2, "王师傅",
                "Y轴导轨润滑不足导致磨损", "清理导轨并重新涂抹润滑油，调整丝杠间隙", "导轨润滑油XHP461×5L", 2);
        createWorkOrder(e9, "赵操作", "滚齿齿形偏差过大", "机械故障", 0, 2, "李维修",
                "刀具磨损导致切削参数偏离", "更换滚刀，重新设定切削参数，首件检验合格后批量", "滚刀 M8×Φ120×1把", 2);
        createWorkOrder(e2, "张管理", "换刀机构卡顿，机械手无法正常抓刀", "传动故障", 2, 2, "王师傅",
                "换刀气缸密封圈老化漏气", "更换换刀气缸密封组件，重新调试机械手位置", "密封圈组套×1", 2);
        createWorkOrder(e6, "张管理", "电气柜频繁报警ERR-22，无法启动", "电气故障", 2, 2, "李维修",
                "伺服驱动器I/O模块接触不良", "重新插拔I/O模块连接器，清理触点，升级驱动固件", null, 2);
        createWorkOrder(e10, "赵操作", "剪切刀片磨损，切口毛刺严重", "机械故障", 0, 2, "王师傅",
                "刀片刃口钝化，间隙调整不当", "更换上下剪切刀片，重新调整剪切间隙至0.08mm", "剪切刀片组×1", 2);
        createWorkOrder(e13, "赵操作", "尾座套筒无法正常伸缩", "机械故障", 0, 0, null, null, null, null, 0);
    }

    private void backfillRatedParams() {
        List<Equipment> all = equipmentRepository.findAll();
        for (Equipment e : all) {
            if (e.getRatedPower() != null) continue;
            String model = e.getModel();
            if (model == null) continue;
            switch (model) {
                case "CK6150" -> { e.setRatedSpindleSpeed(1400.0); e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(70.0); }
                case "VMC850" -> { e.setRatedSpindleSpeed(8000.0); e.setRatedPower(11.0); e.setRatedCurrent(24.0); e.setNormalTempMax(75.0); }
                case "XK7132" -> { e.setRatedSpindleSpeed(6000.0); e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(70.0); }
                case "M1432"  -> { e.setRatedSpindleSpeed(3000.0); e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(65.0); }
                case "HP300"  -> { e.setRatedSpindleSpeed(500.0);  e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(70.0); }
                case "TP619"  -> { e.setRatedSpindleSpeed(2000.0); e.setRatedPower(11.0); e.setRatedCurrent(24.0); e.setNormalTempMax(70.0); }
                case "X2020"  -> { e.setRatedSpindleSpeed(6000.0); e.setRatedPower(15.0); e.setRatedCurrent(32.0); e.setNormalTempMax(75.0); }
                case "T600"   -> { e.setRatedSpindleSpeed(8000.0); e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(70.0); }
                case "Y3150"  -> { e.setRatedSpindleSpeed(2000.0); e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(70.0); }
                case "QC12Y"  -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(60.0); }
                case "D7140"  -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(4.0); e.setRatedCurrent(9.0);  e.setNormalTempMax(65.0); }
                case "WC67Y"  -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(7.5); e.setRatedCurrent(16.0); e.setNormalTempMax(65.0); }
                case "CW6180" -> { e.setRatedSpindleSpeed(800.0);  e.setRatedPower(11.0); e.setRatedCurrent(24.0); e.setNormalTempMax(75.0); }
                case "Z5140"  -> { e.setRatedSpindleSpeed(3000.0); e.setRatedPower(4.0); e.setRatedCurrent(9.0);  e.setNormalTempMax(65.0); }
                case "M6025"  -> { e.setRatedSpindleSpeed(4000.0); e.setRatedPower(3.0); e.setRatedCurrent(7.0);  e.setNormalTempMax(65.0); }
                case "GZ4000" -> { e.setRatedSpindleSpeed(0.0);    e.setRatedPower(5.5); e.setRatedCurrent(12.0); e.setNormalTempMax(60.0); }
                case "HMC630" -> { e.setRatedSpindleSpeed(6000.0); e.setRatedPower(15.0); e.setRatedCurrent(32.0); e.setNormalTempMax(75.0); }
                case "YT32-315" -> { e.setRatedSpindleSpeed(0.0);  e.setRatedPower(15.0); e.setRatedCurrent(32.0); e.setNormalTempMax(70.0); }
            }
            if (e.getRatedPower() != null) {
                equipmentRepository.save(e);
            }
        }
    }

    private User createUser(String username, String password, String realName, int role, String phone) {
        User user = new User(username, passwordEncoder.encode(password), realName, role, phone);
        return userRepository.save(user);
    }

    private Equipment createEquipment(String code, String name, String model, String spec,
                                       String manufacturer, String location, String workshop,
                                       String manager, LocalDate purchaseDate, LocalDate startDate,
                                       int status, Double ratedSpindleSpeed, Double ratedPower,
                                       Double ratedCurrent, Double normalTempMax) {
        Equipment e = new Equipment();
        e.setCode(code);
        e.setName(name);
        e.setModel(model);
        e.setSpec(spec);
        e.setManufacturer(manufacturer);
        e.setLocation(location);
        e.setWorkshop(workshop);
        e.setManager(manager);
        e.setPurchaseDate(purchaseDate);
        e.setStartDate(startDate);
        e.setStatus(status);
        e.setRatedSpindleSpeed(ratedSpindleSpeed);
        e.setRatedPower(ratedPower);
        e.setRatedCurrent(ratedCurrent);
        e.setNormalTempMax(normalTempMax);
        e.setCreatedAt(LocalDateTime.now());
        Equipment saved = equipmentRepository.save(e);
        generateQRCode(saved);
        return saved;
    }

    private void generateQRCode(Equipment equipment) {
        try {
            String qrDir = uploadPath + "/qrcodes";
            new File(qrDir).mkdirs();
            String filePath = qrDir + "/equipment_" + equipment.getId() + ".png";

            String content = "yiweibao://equipment/" + equipment.getId();

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            MatrixToImageWriter.writeToPath(matrix, "PNG", Path.of(filePath));

            equipment.setQrCodePath("/api/files/qrcodes/equipment_" + equipment.getId() + ".png");
            equipmentRepository.save(equipment);
        } catch (Exception e) {
            System.err.println("[DataInitializer] QR code generation failed for equipment " + equipment.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createWorkOrder(Equipment equipment, String reporter, String faultDesc,
                                  String faultCategory, int urgency, int status, String engineer,
                                  String diagnosis, String repairAction, String parts, int hourOffset) {
        WorkOrder wo = new WorkOrder();
        wo.setOrderNo("WO" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", (int) (Math.random() * 1000)));
        wo.setEquipment(equipment);
        wo.setReporter(reporter);
        wo.setFaultDesc(faultDesc);
        wo.setFaultCategory(faultCategory);
        wo.setUrgency(urgency);
        wo.setStatus(status);
        wo.setRepairEngineer(engineer);
        wo.setDiagnosis(diagnosis);
        wo.setRepairAction(repairAction);
        wo.setReplacedParts(parts);
        wo.setCreatedAt(LocalDateTime.now().minusHours(hourOffset));
        if (status == 2) {
            wo.setCompletedAt(LocalDateTime.now().minusHours(hourOffset).plusHours(2 + (long)(Math.random() * 6)));
        }
        workOrderRepository.save(wo);
    }

    private void seedDiagnosisRules() {
        addRule("主轴温度过高预警", "温度超过60°C，存在过热风险",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":60},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "主轴轴承磨损或冷却不足，导致运行温度异常升高", "检查冷却系统管路是否畅通，更换轴承润滑脂，必要时降低加工负荷", "机械故障", 1, 10);
        addRule("主轴严重过热告警", "温度超过70°C，需立即处理",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":70},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "主轴冷却系统故障或长时间过载运行", "立即停机冷却，检查冷却液管路和冷却泵工作状态，排查主轴箱润滑油位", "温控故障", 2, 5);
        addRule("异常振动预警", "振动值超过3.5mm/s",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "刀具安装松动或主轴动平衡不良", "检查刀具夹紧状态，进行主轴动平衡测试，紧固地脚螺栓", "机械故障", 1, 15);
        addRule("严重振动告警", "振动值超过5.5mm/s",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":5.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "主轴轴承损坏或切削参数严重异常", "立即停机，检查主轴轴承游隙和滚动体状态，重新校验切削参数", "机械故障", 2, 8);
        addRule("电流过载预警", "电流超过25A",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":25},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "切削负载过大或电机绕组异常", "降低进给速度，检查电机三相电流平衡，测量绕组绝缘电阻", "电气故障", 1, 20);
        addRule("功率异常预警", "功率超过12kW",
                "[[{\"field\":\"power\",\"operator\":\">\",\"value\":12},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "设备过载运行或传动系统阻力增大", "检查传动皮带张紧度和齿轮箱润滑油位，适当降低生产节拍", "传动故障", 1, 25);
        addRule("液压压力不足预警", "液压压力低于4.0MPa",
                "[[{\"field\":\"pressure\",\"operator\":\"<\",\"value\":4.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压泵故障或管路存在泄漏", "检查液压油位和油质，排查管路密封性，测试液压泵出口压力", "液压故障", 1, 18);
        addRule("液压压力过高预警", "液压压力超过8.5MPa",
                "[[{\"field\":\"pressure\",\"operator\":\">\",\"value\":8.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压阀卡滞或回油管路堵塞", "清洗液压阀芯，检查回油管路畅通性和溢流阀设定值", "液压故障", 1, 22);
        addRule("轴承早期磨损综合预警", "温度和振动同时偏高",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":60},{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "轴承早期磨损，润滑状态持续恶化", "计划检修轴承，加强润滑频率，持续监测振动趋势变化", "机械故障", 1, 12);
        addRule("主轴即将失效告警", "温度超过70°C且振动超过5.0mm/s",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":70},{\"field\":\"vibration\",\"operator\":\">\",\"value\":5.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "主轴轴承严重损坏，随时可能失效", "立即停机，安排更换主轴轴承总成，严禁继续运行", "机械故障", 2, 1);
        addRule("转速异常预警", "主轴转速低于500rpm",
                "[[{\"field\":\"spindleSpeed\",\"operator\":\"<\",\"value\":500}]]",
                "变频器参数异常或主传动皮带打滑", "检查变频器频率设定和输出电流，检查主传动皮带张紧度和磨损情况", "传动故障", 1, 30);
        addRule("电机电气综合异常告警", "电流超过22A且功率超过10kW",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":22},{\"field\":\"power\",\"operator\":\">\",\"value\":10}]]",
                "电机绕组老化或三相电压严重不平衡", "检测电机三相电流和绝缘电阻，检查电源电压平衡性", "电气故障", 2, 10);
        addRule("冷却系统严重故障告警", "温度超过70°C且设备告警",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":70},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "冷却系统完全失效或主轴箱存在抱死风险", "紧急停机，全面检查冷却泵、管路及主轴箱润滑油路", "温控故障", 2, 3);
        addRule("刀具磨损综合预警", "振动超过4.0mm/s且电流超过20A",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":4.0},{\"field\":\"current\",\"operator\":\">\",\"value\":20}]]",
                "刀具磨损严重或切削参数设置不当", "更换刀具并重新校验进给量和切削速度", "机械故障", 1, 16);
        addRule("设备严重综合故障告警", "温度、振动、电流、功率四重异常",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":65},{\"field\":\"vibration\",\"operator\":\">\",\"value\":4.0},{\"field\":\"current\",\"operator\":\">\",\"value\":20},{\"field\":\"power\",\"operator\":\">\",\"value\":10}]]",
                "设备多系统同时异常，可能存在机械结构损伤", "立即停机，通知设备管理部门组织全面检修", "机械故障", 2, 2);
    }

    private void addRule(String name, String desc, String conditionJson, String cause, String action,
                         String category, int severity, int priority) {
        DiagnosisRule rule = new DiagnosisRule();
        rule.setName(name);
        rule.setSymptomDescription(desc);
        rule.setConditionJson(conditionJson);
        rule.setPossibleCause(cause);
        rule.setRecommendedAction(action);
        rule.setFaultCategory(category);
        rule.setSeverityLevel(severity);
        rule.setPriority(priority);
        diagnosisRuleRepository.save(rule);
    }
}
