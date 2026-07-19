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
        // === 机械故障 (15 rules) ===
        addRichRule("主轴轴承磨损预警", "主轴运转时有异响，温度缓慢升高，振动值逐渐增大",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":60},{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "主轴前轴承长期运行导致滚动体疲劳磨损，润滑脂老化失效", "更换主轴前轴承组，使用耐高温锂基润滑脂重新润滑，调整轴承预紧力至标准值",
                "机械故障", 1, 10,
                "主轴,轴承,异响,温升,振动增大,磨损",
                "[\"停机并切断电源，等待主轴完全停止转动\",\"拆除主轴前端盖和密封件\",\"用拉马拆卸旧轴承，检查主轴轴颈有无划伤\",\"清洁轴承座并测量配合公差\",\"安装新轴承并加注指定牌号润滑脂\",\"重新安装端盖并调整轴承预紧力\",\"低速试运行30分钟监测温度和振动\"]",
                "轴承拉马,千分表,扭矩扳手,耐高温润滑脂,清洁布,密封垫片",
                "拆卸前必须确认主轴已完全停止并切断总电源；轴承安装时严禁直接敲击滚动体；更换后必须进行低速磨合",
                4.0, "CK6150,VMC850,XK7132,M1432,TP619,T600,HMC630");

        addRichRule("主轴严重磨损告警", "主轴振动剧烈超过5.5mm/s，温度超过70°C，加工表面有明显振纹",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":70},{\"field\":\"vibration\",\"operator\":\">\",\"value\":5.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "主轴轴承严重损坏，滚动体剥落或保持架碎裂，轴颈可能已损伤", "立即停机！严禁继续运行。需拆卸主轴总成，送专业维修车间检查轴颈状况，必要时更换整套主轴轴承组并修复轴颈",
                "机械故障", 2, 1,
                "主轴,剧烈振动,高温,振纹,轴承碎裂,紧急停机",
                "[\"立即按下急停按钮，切断设备总电源\",\"悬挂'禁止合闸'警示牌\",\"拆卸主轴总成并送至维修车间\",\"测量主轴轴颈跳动量和圆柱度\",\"如轴颈损伤则上磨床修磨或激光熔覆修复\",\"更换全套主轴轴承并调整间隙\",\"主轴总成回装后使用激光对中仪校准\",\"进行8小时磨合运转并全程记录振动/温度数据\"]",
                "急停按钮,警示牌,拉马,千分表,激光对中仪,全套主轴轴承,吊装设备",
                "高压危险！必须切断总电源并验电；主轴总成重量大，吊装时必须使用合格吊具并有专人指挥；修复后未通过磨合测试严禁投入生产",
                12.0, "CK6150,VMC850,XK7132,HMC630,CW6180");

        addRichRule("导轨磨损卡滞", "工作台或刀架移动不畅，有爬行现象，定位精度下降",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.5},{\"field\":\"current\",\"operator\":\">\",\"value\":18},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "导轨润滑不良导致滑动面磨损，或切屑进入导轨面造成拉伤", "清理导轨面切屑和油污，检查刮油片和防护罩完好性，修复轻微拉伤，重新调整镶条间隙并充分润滑",
                "机械故障", 1, 12,
                "导轨,卡滞,爬行,定位精度,润滑,拉伤",
                "[\"清理导轨表面切屑和油污\",\"检查导轨防护罩和刮油片是否损坏\",\"用油石打磨轻微拉伤部位\",\"检查自动润滑系统出油是否正常\",\"调整镶条间隙至0.02-0.04mm\",\"低速全行程往复运行检查有无卡滞\"]",
                "油石,塞尺,润滑油枪,清洁布,导轨油",
                "清理导轨时注意切屑锋利可能割伤手；调整镶条时需多次测量防止过紧抱死",
                3.0, "CK6150,XK7132,M1432,TP619,X2020,CW6180");

        addRichRule("滚珠丝杠间隙过大", "数控轴反向间隙超差，加工尺寸不稳定，重复定位精度差",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "滚珠丝杠长期使用导致滚道和滚珠磨损，或丝杠螺母副预紧力下降", "测量反向间隙值，调整丝杠螺母副预紧力或双螺母垫片，如间隙超过0.05mm则更换丝杠副",
                "机械故障", 1, 15,
                "丝杠,反向间隙,定位精度,尺寸不稳定,滚珠磨损",
                "[\"用千分表测量各轴反向间隙并记录\",\"检查丝杠两端轴承座是否松动\",\"拆下丝杠护罩检查滚道和滚珠状态\",\"如间隙<0.03mm则调整双螺母垫片\",\"如间隙>0.05mm则更换丝杠螺母副\",\"更换后重新进行激光干涉仪补偿\"]",
                "千分表,磁性表座,塞尺,激光干涉仪,内六角扳手套装",
                "更换丝杠后必须重新进行螺距误差补偿和反向间隙补偿；拆装过程中注意保护丝杠滚道不受磕碰",
                6.0, "VMC850,XK7132,T600,HMC630");

        addRichRule("联轴器弹性体失效", "主轴启动时有冲击声，运转中转速波动，联轴器部位有异响",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"spindleSpeed\",\"operator\":\"<\",\"value\":800},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "电机与主轴间联轴器弹性体老化碎裂，或联轴器紧固螺栓松动", "检查联轴器弹性体磨损碎裂情况，更换弹性体组件，重新紧固联轴器螺栓并校验同轴度",
                "机械故障", 1, 20,
                "联轴器,弹性体,冲击声,转速波动,异响,同轴度",
                "[\"拆卸联轴器防护罩\",\"检查弹性体有无裂纹、变形、缺失\",\"用百分表检查电机轴与主轴的同轴度\",\"松开联轴器紧固螺栓并更换弹性体\",\"按对角顺序逐步紧固螺栓至规定扭矩\",\"安装防护罩后试运行\"]",
                "百分表,磁性表座,扭矩扳手,弹性体备件,内六角扳手",
                "拆卸前必须断电并确认主轴完全停止；联轴器螺栓必须按对角顺序紧固，扭矩需均匀",
                2.0, "CK6150,XK7132,M1432,TP619");

        addRichRule("齿轮箱异常磨损", "齿轮箱区域有明显金属摩擦声或周期性敲击声，油液中含有金属粉末",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":4.0},{\"field\":\"temperature\",\"operator\":\">\",\"value\":55},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "齿轮齿面点蚀、剥落或断齿，润滑油变质或油位不足加剧磨损", "放油检查油液金属含量，用内窥镜检查齿面状态，轻微点蚀可继续监测，严重剥落或断齿需拆箱更换齿轮",
                "机械故障", 2, 6,
                "齿轮箱,异响,金属粉末,点蚀,剥落,油液变质",
                "[\"停机冷却后从放油口取样检查油液\",\"用磁塞检查铁屑含量和粒度\",\"拆下观察窗用内窥镜检查齿面\",\"轻微点蚀: 更换新油并添加抗磨剂继续监测\",\"严重损伤: 拆卸齿轮箱更换损坏齿轮\",\"更换所有密封垫片并加注新油至标准油位\"]",
                "内窥镜,油液取样瓶,磁塞,齿轮油,密封垫片,吊装设备",
                "放油时油温可能较高注意防烫；齿轮箱拆卸需专业人员操作；旧油必须按环保要求处理",
                8.0, "CK6150,Y3150,M1432,X2020");

        addRichRule("刀具严重磨损", "加工表面粗糙度明显增大，切削时有刺耳噪声，切屑颜色变蓝",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":4.0},{\"field\":\"current\",\"operator\":\">\",\"value\":20},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "刀具达到使用寿命极限，刃口钝化或崩刃，切削参数与刀具不匹配", "立即更换刀具，检查刀片/刀柄安装面清洁度，重新设定切削参数（降低进给量或切削深度），检查冷却液喷射方向",
                "机械故障", 1, 16,
                "刀具,磨损,粗糙度,噪声,切屑,刃口钝化,崩刃",
                "[\"停机并确认主轴完全停止\",\"卸下旧刀具检查刀片磨损形态\",\"清洁刀柄锥面和主轴锥孔\",\"安装新刀具并确认夹紧到位\",\"调整冷却液喷嘴对准切削区\",\"降低进给量20%试切首件检验\"]",
                "刀具扳手,刀片,清洁布,锥孔清洁刷,粗糙度仪",
                "更换刀具时注意刀片刃口锋利可能割伤；刀具安装必须确认拉钉已锁紧；试切时操作人员不得靠近旋转部件",
                0.5, "CK6150,VMC850,XK7132,TP619,T600,HMC630");

        addRichRule("地脚螺栓松动", "设备运转时整体晃动增大，地脚附近有异常声响，加工精度不稳定",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "长期振动导致地脚螺栓松动，或基础灌浆层开裂沉降", "逐个检查并按规定扭矩紧固各地脚螺栓，检查基础有无裂纹，必要时重新灌浆或加垫铁调平",
                "机械故障", 2, 5,
                "地脚螺栓,松动,晃动,基础,精度,调平",
                "[\"用水平仪检查设备纵横水平度\",\"逐个检查地脚螺栓紧固状态\",\"用规定扭矩重新紧固所有地脚螺栓\",\"检查基础有无裂纹或沉降痕迹\",\"如基础沉降需加垫铁重新调平\",\"调平后再次紧固并点焊固定垫铁\"]",
                "水平仪,扭矩扳手,垫铁,电焊机,塞尺",
                "紧固地脚螺栓时必须按对角顺序逐步加力；使用梯子或平台时注意防坠落；电焊作业需持证操作并配备灭火器",
                4.0, "X2020,CW6180,Y3150,HP300,YT32-315");

        addRichRule("卡盘夹紧力不足", "工件加工时有打滑现象，装夹后工件可手动转动，加工尺寸偏差大",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.0},{\"field\":\"pressure\",\"operator\":\"<\",\"value\":4.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压卡盘油缸密封老化导致压力内泄，或卡爪磨损导致夹持面接触不良", "检查液压卡盘系统压力，更换油缸密封件，修磨或更换卡爪，调整夹紧力至标准值",
                "机械故障", 1, 18,
                "卡盘,夹紧力,打滑,油缸,密封,卡爪磨损",
                "[\"用压力表检测卡盘油缸工作压力\",\"检查液压管路有无外泄漏\",\"拆卸油缸检查密封件状态\",\"更换老化密封件并清洗油缸内部\",\"检查卡爪夹持面磨损情况\",\"修磨卡爪或更换\",\"组装后多次夹紧/松开测试\"]",
                "压力表,密封件套组,液压油,卡爪,油石,内六角扳手",
                "拆卸液压管路前必须卸压；更换密封件后需反复测试夹紧/松开动作确认无泄漏",
                3.0, "CK6150,CW6180");

        addRichRule("砂轮不平衡", "磨削时工件表面出现规律的振纹，磨头部位振动明显，粗糙度超标",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "砂轮磨损不均匀导致动平衡破坏，或砂轮安装法兰盘偏斜", "拆卸砂轮进行动平衡校正，检查砂轮法兰盘安装面有无异物，修整砂轮表面恢复锐度",
                "机械故障", 1, 14,
                "砂轮,不平衡,振纹,动平衡,粗糙度,法兰盘",
                "[\"拆卸砂轮并清洁法兰安装面\",\"将砂轮安装到动平衡仪上测量不平衡量\",\"按测量结果在轻点位置去除等重材料\",\"重新测量确认残余不平衡量在允差内\",\"安装砂轮并修整砂轮工作面\",\"试磨首件检查表面粗糙度\"]",
                "动平衡仪,砂轮扳手,金刚石修整笔,粗糙度仪,清洁布",
                "拆卸砂轮前必须确认主轴完全停止；动平衡操作时注意砂轮边缘锋利；砂轮安装后必须空转5分钟确认无异常",
                2.0, "M1432,M6025");

        addRichRule("冲压模具磨损", "冲压件毛刺增大，断面质量下降，冲压声音异常",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"current\",\"operator\":\">\",\"value\":18},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "冲压模具刃口钝化、崩口或间隙超差，模具导向件磨损", "拆模检查刃口状态，轻微钝化可刃磨修复，严重崩口需更换模具镶块，重新调整凸凹模间隙",
                "机械故障", 1, 17,
                "模具,磨损,毛刺,刃口,间隙,冲压",
                "[\"停机确认飞轮完全停止并锁定\",\"拆卸模具检查凸凹模刃口\",\"测量凸凹模间隙是否在0.05-0.08mm范围内\",\"刃磨钝化刃口至锋利状态\",\"更换损坏的模具导向件\",\"重新组装并调整间隙\",\"试冲10件检查毛刺和断面质量\"]",
                "吊装设备,刃磨机,塞尺,内六角扳手,模具润滑油,千分尺",
                "拆模必须锁定滑块防止意外下落；模具重量大需使用吊装设备；刃磨时佩戴防护眼镜",
                4.0, "HP300,WC67Y,QC12Y");

        addRichRule("剪切刀片钝化", "板材剪切后切口毛刺严重，断面不平整，剪切声音沉闷",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":18},{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "剪切刀片刃口长期使用钝化，上下刀片间隙调整不当", "更换或刃磨上下剪切刀片，按板材厚度重新调整剪切间隙，检查刀片安装面有无异物",
                "机械故障", 1, 19,
                "剪切,刀片,毛刺,刃口钝化,间隙,断面",
                "[\"停机并锁定刀架防止意外下落\",\"拆下上下刀片检查刃口状态\",\"轻微钝化: 上磨床刃磨至规定角度\",\"严重损坏: 更换新刀片组\",\"清洁刀片安装面并重新安装\",\"按板材厚度调整上下刀片间隙\",\"试剪板材首件检验切口质量\"]",
                "吊装设备,刃磨机,塞尺,扭矩扳手,毛刺检测规",
                "拆卸刀片时注意刃口极其锋利必须戴防割手套；吊装刀片时下方严禁站人；调整间隙后必须反复确认",
                6.0, "QC12Y");

        addRichRule("尾座套筒卡滞", "尾座套筒伸缩困难或完全卡死，手轮转动费力",
                "[[{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "尾座套筒内孔拉伤或锈蚀，润滑脂干涸导致滑动面摩擦增大", "拆卸尾座套筒，检查并修磨拉伤部位，清洁内孔并重新润滑，如套筒变形严重则更换",
                "机械故障", 1, 30,
                "尾座,套筒,卡滞,拉伤,润滑,锈蚀",
                "[\"摇出套筒至最大行程\",\"拆卸尾座手轮和锁紧机构\",\"取出套筒检查表面有无拉伤锈蚀\",\"用油石修磨轻微拉伤部位\",\"清理套筒内孔并加注新润滑脂\",\"回装并测试伸缩是否顺畅\"]",
                "油石,润滑脂,内六角扳手,清洁布,防锈油,千分表",
                "拆卸时注意套筒可能突然滑出造成夹伤；回装后需检查套筒轴线与主轴轴线的同轴度",
                2.0, "CK6150,CW6180");

        addRichRule("工作台平面度超差", "加工后工件平面度检测不合格，工作台局部有可见磨损痕迹",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "长期使用导致工作台面局部磨损或变形，切屑或杂物进入工作台与工件之间造成压痕", "用平板和塞尺检测工作台平面度，轻微超差可上磨床修磨，严重变形需更换工作台",
                "机械故障", 2, 8,
                "工作台,平面度,磨损,变形,精度",
                "[\"彻底清理工作台表面\",\"用标准平板和塞尺检测平面度\",\"用千分表沿对角线多点测量记录\",\"定位超差区域并标记\",\"轻微超差: 上导轨磨床修磨\",\"修磨后重新检测确认合格\",\"调整夹具定位销位置补偿剩余误差\"]",
                "标准平板,塞尺,千分表,磁性表座,导轨磨床,记号笔",
                "大型工作台吊装搬运时注意安全；磨削加工需冷却充分防止热变形",
                8.0, "VMC850,XK7132,T600,X2020");

        // === 电气故障 (10 rules) ===
        addRichRule("电机三相电流不平衡", "电机运行时有电磁异响，三相电流差值超过10%，电机外壳温度偏高",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":22},{\"field\":\"temperature\",\"operator\":\">\",\"value\":65},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "电源电压三相不平衡，或电机绕组匝间短路、接线端子松动氧化", "用钳形表测量三相电流和电压，检查接线端子紧固状态，用兆欧表测量绕组绝缘电阻和直流电阻平衡度",
                "电气故障", 2, 5,
                "电机,三相不平衡,电流,电压,绕组,绝缘,接线",
                "[\"停机并切断电源验电确认\",\"打开电机接线盒检查端子有无氧化松动\",\"用钳形表测量三相电流记录差值\",\"用万用表测量三相电源电压\",\"用兆欧表测量绕组对地绝缘电阻(应>1MΩ)\",\"用直流电阻测试仪测量三相电阻平衡度\",\"如绝缘不合格需烘烤或重新浸漆\",\"如匝间短路需更换电机\"]",
                "钳形电流表,万用表,兆欧表,直流电阻测试仪,绝缘胶带,接线端子",
                "高压危险！测量前必须验电并悬挂警示牌；兆欧表测试电压可能高达500V/1000V，测试时不要触碰测试线",
                3.0, "CK6150,VMC850,XK7132,TP619,X2020,M1432");

        addRichRule("伺服驱动器报警", "数控系统显示伺服报警代码，对应轴无法运动或运动异常",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":20},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "伺服驱动器检测到过流、过压、编码器异常或通信故障，IGBT模块可能损坏", "记录报警代码查阅手册，检查伺服电机动力线和编码器线连接，测量驱动器输入输出电压，断电后检查IGBT模块",
                "电气故障", 2, 3,
                "伺服,驱动器,报警,编码器,IGBT,过流,通信故障",
                "[\"记录驱动器面板显示的报警代码\",\"查阅驱动器手册确定报警原因\",\"检查驱动器和电机间的动力线/编码器线是否松动\",\"测量驱动器直流母线电压是否正常\",\"断电后用万用表二极管档检测IGBT模块\",\"检查控制电源是否正常\",\"如IGBT损坏需更换驱动器模块\"]",
                "万用表,示波器,驱动器手册,螺丝刀套装,编码器线备件",
                "驱动器内部有大容量电解电容，断电后需等待5分钟以上待电容放电完毕再操作；更换驱动器模块前必须确认型号和参数匹配",
                4.0, "VMC850,XK7132,T600,HMC630");

        addRichRule("接触器触头烧蚀", "设备启动困难或运行中突然停机，接触器吸合时有拉弧声",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":25},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "接触器触头长期通断产生电弧烧蚀，触头表面氧化导致接触电阻增大发热", "断电后检查接触器触头烧蚀程度，轻微发黑可用细砂纸打磨，严重凹坑需更换接触器，同时检查线圈电压是否正常",
                "电气故障", 1, 15,
                "接触器,触头,烧蚀,拉弧,启动困难,线圈",
                "[\"切断总电源并验电\",\"拆卸接触器灭弧罩\",\"检查主触头和辅助触头表面状态\",\"轻微氧化: 用细砂纸打磨平整\",\"严重烧蚀: 记录型号更换新接触器\",\"测量线圈电阻和绝缘电阻\",\"检查线圈控制电压是否在额定范围\",\"重新安装并测试吸合/释放\"]",
                "万用表,细砂纸,螺丝刀套装,备用接触器,验电器",
                "更换接触器必须断电验电；注意区分线圈电压等级(AC220V/DC24V等)；更换后必须测试紧急停止功能",
                1.5, "VMC850,XK7132,HP300,QC12Y,X2020");

        addRichRule("编码器信号异常", "数控轴位置显示跳变，加工中出现走刀偏差，系统偶发位置偏差报警",
                "[[{\"field\":\"spindleSpeed\",\"operator\":\"<\",\"value\":500},{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "编码器电缆屏蔽层损坏导致信号干扰，编码器码盘污染或光源老化，机械连接松动", "检查编码器电缆屏蔽接地，清洁编码器内部码盘，紧固机械连接，用示波器检测输出信号波形",
                "电气故障", 2, 8,
                "编码器,信号,跳变,位置偏差,干扰,码盘,屏蔽",
                "[\"检查编码器电缆外观有无破损\",\"测量电缆屏蔽层接地电阻(<1Ω)\",\"拆卸编码器检查码盘有无污染\",\"用无尘布和无水酒精清洁码盘\",\"检查编码器与电机的机械连接是否松动\",\"用示波器检测A/B/Z相输出信号\",\"信号异常则更换编码器并重新校准零点\"]",
                "示波器,万用表,无尘布,无水酒精,内六角扳手,备用编码器",
                "清洁码盘需在洁净环境中操作防止二次污染；更换编码器后必须重新设置参考点和进行螺距补偿",
                3.0, "VMC850,XK7132,T600,HMC630");

        addRichRule("PLC模块故障", "设备部分功能失效（如润滑、排屑、刀库等），PLC指示灯异常",
                "[[{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "PLC输入/输出模块损坏，通信总线故障，或24V控制电源异常", "根据PLC指示灯状态判断故障范围，检查24V控制电源电压，更换故障I/O模块，检查总线终端电阻",
                "电气故障", 2, 4,
                "PLC,模块,IO,通信,控制电源,总线,指示灯",
                "[\"查看PLC CPU和I/O模块指示灯状态\",\"用编程软件连接PLC查看诊断信息\",\"测量24V直流控制电源是否稳定\",\"检查通信总线接头和终端电阻\",\"逐模块排查找到故障I/O模块\",\"更换故障模块并重新下载程序(如有需要)\",\"测试所有相关功能确认恢复\"]",
                "万用表,PLC编程电缆,笔记本电脑,备用I/O模块,螺丝刀",
                "更换PLC模块前必须断电；注意模块型号和版本必须兼容；换模块后功能测试必须覆盖所有相关安全联锁",
                3.0, "VMC850,HMC630,T600,X2020");

        addRichRule("变频器过载保护", "变频器显示过载报警代码，主轴无法启动或自动降速",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":30},{\"field\":\"temperature\",\"operator\":\">\",\"value\":60},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "机械负载过大导致电机电流超限，变频器参数设置不当，或变频器散热不良", "检查机械负载是否正常，查看变频器历史报警记录，测量输出电流和电压波形，清洁变频器散热风道",
                "电气故障", 2, 6,
                "变频器,过载,报警,电流,负载,散热,参数",
                "[\"查看变频器面板显示的报警号和输出电流值\",\"检查机械部分有无卡滞导致过载\",\"用钳形表核实输出电流与显示值是否一致\",\"检查变频器散热风扇是否运转\",\"清理散热器翅片和风道积尘\",\"检查加减速时间和V/F曲线参数是否合理\",\"复位报警后低速启动逐步升速\"]",
                "钳形表,万用表,压缩空气,变频器手册,备用散热风扇",
                "变频器内部有大电容，断电后等待放电完毕(>10分钟)；修改参数前必须记录原参数值；散热风扇每月应检查一次",
                2.0, "CK6150,VMC850,M1432,TP619,CW6180");

        addRichRule("限位开关失效", "工作台或刀架超越行程极限未停止，或误触发限位导致无法回参考点",
                "[[{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "限位开关机械触点磨损或卡死，接近开关感应面污染，或线路断路短路", "检查限位开关动作是否灵活，接近开关感应距离是否正常，测量开关通断和线路通断",
                "电气故障", 2, 2,
                "限位开关,超程,参考点,接近开关,触点,感应",
                "[\"手动移动轴到限位位置观察开关是否动作\",\"检查机械限位撞块是否松动移位\",\"用万用表测量限位开关触点通断\",\"清洁接近开关感应面油污\",\"调整接近开关感应距离至2-4mm\",\"检查限位开关线路有无断线\",\"更换故障开关并重新设置软限位\"]",
                "万用表,塞尺,螺丝刀,备用限位开关,备用接近开关,清洁布",
                "测试限位功能时必须低速操作防止撞机；超程释放后必须重新回参考点；软限位参数不得超出硬限位范围",
                1.5, "VMC850,XK7132,M1432,T600");

        addRichRule("接地不良干扰", "设备偶尔出现不明原因的误动作或报警，触摸设备外壳有麻电感",
                "[[{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "设备接地线松动断裂或接地电阻超标，变频器/伺服驱动器产生的电磁干扰无法有效泄放", "检查设备接地线连接状态，用接地电阻测试仪测量接地电阻，确保所有电气部件可靠接地",
                "电气故障", 2, 10,
                "接地,干扰,误动作,麻电,接地电阻,电磁干扰",
                "[\"目视检查设备接地线和接地排连接\",\"用接地电阻测试仪测量接地电阻(应<4Ω)\",\"检查各电气柜门跨接线是否完好\",\"确认变频器/伺服驱动器PE端子已接地\",\"检查屏蔽电缆屏蔽层是否360°接地\",\"紧固所有接地端子并做防松处理\",\"重新测量接地电阻确认合格\"]",
                "接地电阻测试仪,扭矩扳手,万用表,接地线,防松垫圈",
                "测量接地电阻时辅助接地棒需插入潮湿土壤；禁止将接地线接在暖气管或自来水管上；高压设备接地必须由持证电工操作",
                2.0, "VMC850,X2020,HMC630,HP300,YT32-315");

        addRichRule("电气柜散热不良", "电气柜内温度过高，变频器/驱动器偶尔报过热，夏季高温时故障频发",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":55},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "电气柜散热风扇故障或过滤网堵塞，柜内空气流通不畅导致热量积聚", "检查柜内散热风扇运转情况，清洗或更换过滤网，确保柜内风道畅通，必要时加装机柜空调",
                "电气故障", 1, 20,
                "电气柜,散热,风扇,过滤网,高温,过热",
                "[\"检查电气柜散热风扇是否全部正常运转\",\"拆下过滤网用压缩空气或水清洗\",\"检查柜内风道是否被元器件阻挡\",\"测量柜内温度并与环境温度对比\",\"更换故障风扇\",\"如散热仍不足考虑加装机柜空调\"]",
                "万用表,压缩空气,温度计,备用风扇,过滤网,螺丝刀",
                "清洁过滤网前需断电；禁止用湿布擦拭带电的电气元件；风扇更换注意气流方向要与原方向一致",
                1.5, "VMC850,HMC630,X2020");

        // === 温控故障 (8 rules) ===
        addRichRule("冷却液流量不足", "加工时主轴温度持续升高，冷却液流量明显减小，切屑堆积",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":65},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "冷却液管路堵塞，冷却泵叶轮磨损，或冷却液过滤器堵塞导致流量下降", "清理冷却液管路和喷嘴，检查冷却泵运转情况，清洗或更换过滤器滤芯，检查冷却液液位和质量",
                "温控故障", 1, 12,
                "冷却液,流量,堵塞,温升,冷却泵,过滤器",
                "[\"检查冷却液箱液位是否在正常范围\",\"拆下冷却液喷嘴检查是否堵塞\",\"检查冷却泵运转电流是否正常\",\"拆卸并清洗过滤器滤芯\",\"用压缩空气吹通冷却管路\",\"更换变质或使用超期的冷却液\",\"启动冷却泵检查各喷嘴出液情况\"]",
                "压缩空气,扳手,冷却液,过滤器滤芯,钳形表",
                "更换冷却液时注意冷却液可能含切削碎片不要直接手捞；废冷却液需按环保要求处理不得直接排放",
                2.0, "CK6150,VMC850,XK7132,T600,TP619");

        addRichRule("冷却泵故障", "冷却液完全不出液或出液断续，冷却泵有异常噪声",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":70},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "冷却泵电机烧毁，叶轮脱落或卡死，泵体密封漏水导致电机进水", "检查冷却泵电机是否通电运转，拆卸检查叶轮和密封件，如电机烧毁则更换冷却泵总成",
                "温控故障", 2, 3,
                "冷却泵,故障,不出液,异响,电机,密封",
                "[\"检查冷却泵电机是否通电(听声音/摸振动)\",\"测量电机电流判断是否过载或烧毁\",\"拆卸泵体检查叶轮有无卡死脱落\",\"检查机械密封有无漏水痕迹\",\"如密封漏水更换密封件\",\"如电机烧毁更换冷却泵总成\",\"重新安装并加注冷却液试运行\"]",
                "万用表,钳形表,内六角扳手,备用冷却泵,密封件,冷却液",
                "更换冷却泵必须断电；泵体拆卸时可能有残留冷却液流出注意防滑；新泵安装后需排气防止气蚀",
                3.0, "CK6150,VMC850,M1432,XK7132");

        addRichRule("润滑油路堵塞", "导轨或丝杠润滑不足，自动润滑系统报警或出油量不足",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":55},{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "润滑油路中杂质堵塞分配器或油管，润滑泵压力不足，或油品变质产生胶质", "检查润滑泵出口压力，逐个检查分配器出油情况，清洗或更换堵塞的分配器和油管，更换变质润滑油",
                "温控故障", 1, 14,
                "润滑,油路,堵塞,分配器,润滑油,变质",
                "[\"检查自动润滑泵油位和油品状态\",\"手动启动润滑泵观察压力表读数\",\"逐个检查各润滑点分配器动作情况\",\"拆卸堵塞的分配器用柴油清洗\",\"用压缩空气吹通润滑管路\",\"更换变质或污染的润滑油\",\"重新组装并手动打油确认各点出油正常\"]",
                "压力表,柴油,压缩空气,分配器备件,润滑油,扳手",
                "高压油路拆卸时注意油液可能喷射伤眼需佩戴防护眼镜；润滑油牌号必须与设备要求一致",
                3.0, "VMC850,XK7132,X2020,M1432,HMC630");

        addRichRule("液压油温过高", "液压系统运行一段时间后油温超过60°C，系统响应变慢",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":60},{\"field\":\"pressure\",\"operator\":\">\",\"value\":8.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压油冷却器散热片堵塞或冷却水流量不足，溢流阀长时间溢流，或油箱油位过低", "清洗液压油冷却器散热片，检查冷却水管路和水阀，调整溢流阀压力，补充液压油至标准油位",
                "温控故障", 1, 16,
                "液压油,油温高,冷却器,溢流阀,散热,油位",
                "[\"测量液压油箱实际油温\",\"检查油箱油位和油液颜色(发黑=过热氧化)\",\"清洗冷却器散热片表面灰尘油污\",\"检查冷却水阀是否全开和管路有无堵塞\",\"检查溢流阀设定压力是否过低导致常溢流\",\"如油液氧化变质则更换全部液压油\",\"重新设定溢流阀压力至额定值\"]",
                "红外测温枪,压力表,扳手,液压油,清洁刷,冷却器清洗剂",
                "测量油温时注意液压管路可能很烫；更换液压油需停机冷却后进行；废液压油需按环保要求处理",
                2.5, "HP300,YT32-315,WC67Y");

        addRichRule("主轴油冷机故障", "主轴温度持续升高，油冷机报警或制冷效果差",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":70},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "油冷机压缩机故障、制冷剂泄漏，或冷却油路堵塞", "检查油冷机压缩机运转和制冷剂压力，清理油冷机散热器，检查油路循环泵",
                "温控故障", 2, 2,
                "油冷机,主轴,制冷,压缩机,制冷剂,散热器",
                "[\"查看油冷机面板是否有报警代码\",\"检查油冷机电源和压缩机是否运转\",\"测量制冷剂高低压压力判断是否泄漏\",\"检查冷却油循环管路温度\",\"清理油冷机散热器翅片\",\"检查循环泵运转是否正常\",\"如制冷剂泄漏需查漏补焊后重新充注\"]",
                "制冷剂压力表组,万用表,钳形表,检漏仪,制冷剂,散热器清洗剂",
                "制冷剂压力检测需持证专业人员操作；制冷剂属于温室气体禁止直接排放；油冷机维修后需运行24小时确认制冷正常",
                6.0, "VMC850,HMC630,X2020");

        addRichRule("冷却液变质发臭", "冷却液有刺鼻异味，加工时产生大量泡沫，工件表面有锈蚀",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":55},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "冷却液使用时间过长细菌滋生，浓度配比不当，或混入导轨油/液压油导致变质", "立即更换全部冷却液，彻底清洗冷却液箱和管路，重新按标准浓度配比添加新冷却液",
                "温控故障", 1, 25,
                "冷却液,变质,异味,细菌,浓度,泡沫,锈蚀",
                "[\"用折光仪检测冷却液浓度是否合格\",\"检查冷却液有无分层、变色、异味\",\"将旧冷却液全部抽出\",\"人工清理冷却液箱底部的沉积物\",\"用清水和杀菌剂循环清洗管路30分钟\",\"排放清洗水后加入新冷却液并调至标准浓度\",\"启动循环确认无泡沫和异味\"]",
                "折光仪,冷却液,杀菌清洗剂,水泵,清洁工具,量筒",
                "废冷却液属于危废禁止直接排放需交由有资质单位处理；清洗时注意防滑；配比冷却液时需佩戴手套",
                3.0, "CK6150,VMC850,XK7132,M1432,T600");

        addRichRule("环境温度过高", "夏季高温天气设备故障率明显上升，多个设备同时出现过热报警",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":65},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "车间通风降温设施不足，设备散热条件恶化导致运行温度超标", "加强车间通风（开启门窗、工业风扇），检查车间空调/冷风机运行状态，高温时段适当降低设备运行负荷",
                "温控故障", 1, 28,
                "环境温度,高温,车间,通风,降温,夏季",
                "[\"测量车间各区域环境温度并记录\",\"检查车间通风设备(排风扇/屋顶风机)是否正常\",\"检查车间空调或冷风机运行状态\",\"开启所有通风降温设施\",\"建议高温时段(12:00-15:00)降低加工负荷\",\"如环境温度持续>38°C建议停产降温\"]",
                "温度计,湿度计,风速计",
                "高温环境下操作人员需注意防暑降温；电风扇不得直吹电气柜内部；空调滤网需每周清洗",
                1.0, "CK6150,VMC850,XK7132,M1432,TP619,T600,HP300");

        // === 液压故障 (8 rules) ===
        addRichRule("液压管路外泄漏", "液压管路接头或软管处有油液渗出/滴落，地面有油渍",
                "[[{\"field\":\"pressure\",\"operator\":\"<\",\"value\":4.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压接头密封圈老化或松动，高压软管外层破损或接头扣压失效", "定位泄漏点，紧固接头或更换密封圈，如软管破损则更换整根软管并重新扣压接头",
                "液压故障", 2, 3,
                "液压,泄漏,管路,接头,密封圈,软管,油渍",
                "[\"清洁并擦干部件表面便于定位泄漏点\",\"设备低压运行观察油液渗出位置\",\"标记泄漏的接头或软管\",\"泄压后紧固接头或更换密封圈\",\"破损软管需整体更换\",\"补充液压油至标准油位\",\"运行测试确认无泄漏\"]",
                "扳手套装,O型圈套件,备用液压软管,液压油,清洁布",
                "检查和维修泄漏前必须确认管路已卸压；液压油泄漏在地面有滑倒风险需立即清理；高压油喷射可能击穿皮肤切勿用手触摸可疑泄漏点",
                2.0, "HP300,YT32-315,WC67Y,QC12Y");

        addRichRule("液压泵磨损异响", "液压泵运行时有不规则金属敲击声，系统压力波动，油液中可见金属粉末",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.5},{\"field\":\"pressure\",\"operator\":\"<\",\"value\":4.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压泵内部柱塞/叶片/齿轮磨损，或进油管路漏气导致气蚀", "检查液压油中金属粉末含量，测量泵出口压力和流量，进油管路密封性，必要时更换液压泵",
                "液压故障", 2, 2,
                "液压泵,异响,磨损,压力波动,金属粉末,气蚀",
                "[\"用听诊器贴近液压泵确认异响来源\",\"从油箱取油样检查金属粉末含量\",\"检查进油管路和滤网有无堵塞或漏气\",\"测量泵出口压力和流量\",\"检查联轴器弹性体是否损坏\",\"如确认泵内部磨损需更换液压泵总成\",\"更换后排气并测试压力和流量\"]",
                "听诊器,压力表,流量计,油液取样瓶,备用液压泵,液压油",
                "更换液压泵必须先卸压断电；新泵安装后必须向泵体内注满油再启动防止干磨；旧泵内的残油需妥善处理",
                5.0, "HP300,YT32-315,WC67Y");

        addRichRule("液压阀芯卡滞", "液压缸动作缓慢或爬行，换向时有冲击，液压阀体温度异常升高",
                "[[{\"field\":\"pressure\",\"operator\":\">\",\"value\":8.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压油污染导致阀芯与阀体间隙卡入杂质，或油液氧化产生胶质粘附阀芯", "拆卸液压阀清洗阀芯和阀体，更换污染的液压油，检查并清洗或更换液压油过滤器",
                "液压故障", 1, 10,
                "液压阀,卡滞,爬行,冲击,阀芯,污染",
                "[\"确认系统已卸压\",\"记录阀的安装方向和位置后拆下\",\"分解阀检查阀芯表面有无划伤拉伤\",\"用柴油或清洗剂清洗阀芯和阀体\",\"用压缩空气吹通所有油路孔\",\"检查阀芯在阀体中滑动是否顺畅\",\"更换液压油和过滤器滤芯\",\"重新安装阀并测试动作是否正常\"]",
                "柴油,清洗剂,压缩空气,内六角扳手,液压油,过滤器滤芯",
                "拆卸液压阀时必须确认系统完全卸压；阀芯与阀体是精密配合件，清洗时不得用硬物刮擦；液压阀油路孔极细注意不要堵塞",
                3.0, "HP300,YT32-315,WC67Y");

        addRichRule("液压油缸密封失效", "油缸活塞杆处渗油，油缸推力不足，保压时活塞缓慢滑移",
                "[[{\"field\":\"pressure\",\"operator\":\"<\",\"value\":3.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "油缸活塞密封圈磨损老化或活塞杆表面拉伤，缸筒内壁磨损导致内泄漏", "拆卸油缸更换全套密封件，检查活塞杆表面有无划伤并修磨，测量缸筒内径圆度",
                "液压故障", 2, 5,
                "油缸,密封,渗油,推力不足,内泄漏,活塞杆",
                "[\"标记油缸安装位置和油管连接\",\"卸压后拆除油管和油缸\",\"将油缸送至维修工位\",\"拆卸缸盖取出活塞和活塞杆\",\"检查活塞杆表面有无划伤、锈蚀\",\"轻微划伤: 用油石修磨\",\"更换全套密封件\",\"测量缸筒内径和圆度\",\"回装并做耐压测试\"]",
                "油石,密封件套组,内径千分尺,油缸拆装工具,液压油,扭矩扳手",
                "拆卸大型油缸需使用吊装设备；油缸内部可能存有高压油，拆卸时注意防护；活塞杆表面粗糙度要求Ra≤0.2μm",
                5.0, "HP300,YT32-315");

        addRichRule("液压过滤器堵塞", "液压系统压力不稳，油泵噪音增大，系统响应迟钝",
                "[[{\"field\":\"pressure\",\"operator\":\"<\",\"value\":4.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压油过滤器滤芯长期未更换，杂质积聚导致通油能力下降", "更换液压油过滤器滤芯，检查油箱内油液清洁度，如油液太脏需同时更换液压油",
                "液压故障", 1, 20,
                "过滤器,堵塞,滤芯,压力不稳,油泵噪音,清洁度",
                "[\"查看过滤器上压差指示器是否变红\",\"确认系统停机卸压\",\"旋下过滤器壳体取出旧滤芯\",\"检查旧滤芯表面杂质类型判断污染来源\",\"安装新滤芯并更换密封圈\",\"检查油箱油液清洁度\",\"如油液过脏需全部更换\"]",
                "过滤器扳手,新滤芯,密封圈,液压油,清洁布",
                "更换滤芯时注意油液可能流出做好接油准备；滤芯壳体安装必须到位防止漏油；旧滤芯为危废需妥善处理",
                1.0, "HP300,YT32-315,WC67Y");

        addRichRule("蓄能器失效", "液压系统压力波动大，保压时间明显缩短，蓄能器充气压力不足",
                "[[{\"field\":\"pressure\",\"operator\":\"<\",\"value\":3.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "蓄能器皮囊破裂或充气阀泄漏导致氮气压力不足", "用充气工具检查蓄能器氮气压力，如皮囊破裂则更换皮囊并重新充氮气至规定压力",
                "液压故障", 2, 8,
                "蓄能器,压力波动,保压,皮囊,氮气,充气阀",
                "[\"确认液压系统已完全卸压\",\"用充气工具测量蓄能器气压\",\"如气压为零或极低则皮囊可能破裂\",\"拆卸蓄能器端盖取出破损皮囊\",\"检查蓄能器内壁有无损伤\",\"安装新皮囊并加注少量液压油润滑\",\"用氮气瓶充气至规定压力(通常为系统压力的60-70%)\",\"安装后测试保压性能\"]",
                "充气工具,氮气瓶,压力表,备用皮囊,扭矩扳手",
                "蓄能器内是高压氮气，拆卸前必须确认液压油侧已卸压；充氮气时严禁使用氧气或其他可燃气体；充气压力不得超过蓄能器铭牌标称值",
                3.0, "HP300,YT32-315");

        addRichRule("液压管路振动异常", "液压管路在运行时明显抖动，管夹松动，管路与其他部件有碰撞声",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "液压管路管夹松动或缺失，液压泵脉动过大，或溢流阀设定不当导致管路共振", "紧固或补齐管路管夹，检查液压泵出口脉动，调整溢流阀设定，必要时在管路上加装减振器",
                "液压故障", 1, 22,
                "管路,振动,管夹,共振,脉动,减振",
                "[\"检查各管路段管夹是否松动或缺失\",\"用手触摸管路感知振动最大位置\",\"紧固松动管夹补充缺失管夹\",\"检查液压泵出口压力脉动是否超标\",\"调整溢流阀设定避开共振区\",\"在高振动管段加装管夹或减振器\",\"检查管路与其他部件之间间隙是否足够\"]",
                "扳手,管夹,减振器,压力表,扎带",
                "管路振动可能导致焊缝开裂或接头松动引发泄漏，必须及时处理；紧固管夹时不要过度压缩管路",
                1.5, "HP300,YT32-315,WC67Y,QC12Y");

        addRichRule("液压油污染乳化", "液压油颜色变白浑浊，油箱底部有水分析出，系统动作迟缓",
                "[[{\"field\":\"pressure\",\"operator\":\"<\",\"value\":4.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "冷却器漏水进入液压系统，或油箱呼吸器损坏导致空气中水分冷凝进入油箱", "立即更换乳化液压油，检查并修复冷却器泄漏，更换油箱呼吸器干燥剂，彻底清洗液压系统",
                "液压故障", 2, 4,
                "液压油,乳化,水分,浑浊,冷却器,呼吸器",
                "[\"从油箱底部取样观察是否有分层和水珠\",\"如确认乳化立即停止液压系统\",\"将乳化液压油全部排出\",\"拆检冷却器进行打压查漏\",\"更换油箱呼吸器并检查干燥剂\",\"用清洗油循环清洗液压系统2-3次\",\"加注新液压油至标准油位\"]",
                "油液取样器,清洗油,新液压油,冷却器打压工具,呼吸器干燥剂",
                "乳化液压油润滑性能急剧下降会加速液压元件磨损，发现后必须立即处理；废油处理需符合环保要求",
                5.0, "HP300,YT32-315");

        // === 传动故障 (9 rules) ===
        addRichRule("主传动皮带打滑", "主轴启动时加速缓慢，重切削时转速明显下降，皮带有焦味",
                "[[{\"field\":\"spindleSpeed\",\"operator\":\"<\",\"value\":500},{\"field\":\"temperature\",\"operator\":\">\",\"value\":55},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "传动皮带长期使用后伸长松弛，或皮带工作面磨损导致摩擦力下降", "检查皮带张紧度和磨损状态，重新张紧或更换皮带，检查带轮磨损和两带轮平行度",
                "传动故障", 1, 15,
                "皮带,打滑,转速下降,张紧,磨损,带轮",
                "[\"拆下皮带护罩\",\"检查皮带工作面有无裂纹、磨损、硬化\",\"用手指按压皮带中部检查张紧度\",\"松弛: 调整电机底座张紧螺栓\",\"磨损严重: 更换皮带(成组皮带需成套更换)\",\"检查带轮槽磨损状态\",\"用直尺检查两带轮平行度\",\"调整后低速试运行确认不打滑\"]",
                "扳手,直尺,张力计,备用皮带,皮带轮槽规",
                "调整皮带张紧度必须在停机断电后进行；皮带过紧会导致轴承过载；多根皮带必须同时更换确保受力均匀",
                2.0, "CK6150,M1432,CW6180");

        addRichRule("齿轮啮合间隙过大", "传动齿轮箱有周期性冲击声，反转时有明显空回，加工精度下降",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "齿轮长期运行后齿面磨损导致啮合间隙超差，或轴承磨损导致齿轮轴位置偏移", "测量齿轮啮合间隙，调整齿轮轴位置或更换磨损超差的齿轮，必要时更换配对齿轮组",
                "传动故障", 2, 6,
                "齿轮,间隙,冲击声,空回,磨损,啮合",
                "[\"打开齿轮箱观察窗或端盖\",\"用塞尺测量齿轮啮合侧隙\",\"用压铅法或百分表法测量齿隙\",\"检查各轴承有无松动或磨损\",\"轻微超差: 调整齿轮轴垫片\",\"严重超差: 更换磨损齿轮\",\"重新调整齿轮啮合并更换齿轮油\"]",
                "塞尺,百分表,铅丝,千分尺,齿轮油,密封垫片",
                "齿轮箱检修必须停机断电并悬挂警示牌；更换齿轮后必须进行不少于4小时的磨合运转",
                8.0, "CK6150,Y3150,M1432,CW6180");

        addRichRule("进给系统卡滞", "自动进给时有停顿或爬行现象，进给力不稳定，手摇进给感觉沉重",
                "[[{\"field\":\"current\",\"operator\":\">\",\"value\":20},{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "进给箱内部齿轮或离合器磨损，光杠/丝杠弯曲或润滑不良，导轨镶条过紧", "检查进给箱各档位啮合情况，测量光杠和丝杠直线度，调整导轨镶条间隙，充分润滑",
                "传动故障", 1, 18,
                "进给,卡滞,爬行,光杠,丝杠,镶条,润滑",
                "[\"脱开进给机构手动摇动检查阻力\",\"逐档测试自动进给观察有无卡滞\",\"检查进给箱油位和油质\",\"用百分表测量光杠丝杠跳动量\",\"调整导轨镶条至合适间隙\",\"检查进给离合器磨损状态\",\"充分润滑各滑动面和传动件\"]",
                "百分表,磁性表座,塞尺,齿轮油,油枪",
                "检查进给机构时必须停机；不要戴手套操作旋转中的手轮防止卷入",
                3.0, "CK6150,CW6180,TP619");

        addRichRule("变速机构换挡困难", "变速手柄操作费力，换挡不到位或跳档，变速时有打齿声",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "变速拨叉磨损变形，齿轮端面倒角磨损，或变速定位弹簧失效", "拆检变速机构，更换磨损的拨叉和定位弹簧，修磨齿轮端面倒角，重新调整变速操纵杆行程",
                "传动故障", 1, 22,
                "变速,换挡,拨叉,跳档,打齿,弹簧",
                "[\"停机断电后操作变速手柄感受阻力\",\"打开变速箱上盖检查拨叉状态\",\"检查各档位定位钢球和弹簧\",\"测量拨叉工作面磨损量\",\"更换磨损超差的拨叉和弹簧\",\"检查齿轮端面啮合倒角\",\"重新调整变速操纵杆连杆长度\"]",
                "内六角扳手,卡簧钳,备用拨叉,弹簧,游标卡尺",
                "打开变速箱盖前必须确认设备已断电；拆下的零件按顺序摆放防止装配错误；装好后每个档位需反复挂挡测试",
                4.0, "CK6150,CW6180");

        addRichRule("联轴器紧固螺栓松动", "联轴器部位有周期性异响，传动效率下降，联轴器发热",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"temperature\",\"operator\":\">\",\"value\":50},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "联轴器紧固螺栓未按规定扭矩紧固或防松措施失效，长期振动导致松动", "检查联轴器所有紧固螺栓，按对角顺序重新紧固至规定扭矩，更换失效的防松垫圈或涂螺纹胶",
                "传动故障", 2, 7,
                "联轴器,螺栓,松动,异响,紧固,防松",
                "[\"停机断电后拆除联轴器护罩\",\"用扭矩扳手逐一检查螺栓紧固状态\",\"检查防松垫圈有无变形失效\",\"按对角顺序分2-3次逐步紧固至规定扭矩\",\"更换变形失效的防松垫圈\",\"必要位置涂中等强度螺纹锁固胶\",\"安装护罩后试运行\"]",
                "扭矩扳手,防松垫圈,螺纹锁固胶,内六角扳手",
                "联轴器螺栓必须按对角顺序均匀紧固；扭矩值参照设备说明书不得过高或过低；更换防松垫圈时注意方向",
                1.5, "CK6150,XK7132,M1432,TP619");

        addRichRule("传动链条松弛", "链条传动时有跳动或爬行，链轮和链条啮合处有异响，链条下垂量过大",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "传动链条长期使用后销轴和套筒磨损导致节距变长，或张紧装置失效", "测量链条伸长量，调整张紧装置或截去1-2节链条，如磨损严重则更换整套链轮链条",
                "传动故障", 1, 20,
                "链条,松弛,跳动,磨损,张紧,链轮",
                "[\"停机后测量链条下垂量\",\"检查链条销轴和套筒的磨损程度\",\"检查链轮齿形有无磨损变尖\",\"轻微松弛: 调整张紧轮位置\",\"一般松弛: 截去1-2节链条(偶数节)\",\"严重磨损: 成套更换链条和链轮\",\"充分润滑链条和链轮\"]",
                "卷尺,链条拆卸工具,备用链条,链轮,链条润滑油",
                "拆卸链条前必须确认设备已断电；新链条安装后必须调整张紧度至规定下垂量；链条运行时严禁伸手触碰",
                2.5, "HP300,QC12Y");

        addRichRule("传动轴弯曲振动", "传动轴运转时径向跳动大，轴承座发热，整机振动增大",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":4.5},{\"field\":\"temperature\",\"operator\":\">\",\"value\":55},{\"field\":\"status\",\"operator\":\">=\",\"value\":2}]]",
                "传动轴受到意外冲击载荷导致弯曲变形，或轴承损坏后继续运行导致轴颈偏磨", "用百分表测量传动轴跳动量，轻微弯曲可校直修复，严重弯曲或轴颈偏磨需更换传动轴",
                "传动故障", 2, 4,
                "传动轴,弯曲,振动,跳动,轴承,校直",
                "[\"用百分表测量传动轴各段径向跳动\",\"标记最大跳动位置和方向\",\"检查轴承座温度和振动\",\"拆卸传动轴检查轴颈和弯曲状况\",\"轻微弯曲(<0.05mm): 压力机冷校直\",\"严重弯曲或轴颈损伤: 更换传动轴\",\"更换两端轴承\",\"回装后用百分表重新校调\"]",
                "百分表,磁性表座,压力机,千分尺,V形铁,备用传动轴,轴承",
                "传动轴拆卸前需做好标记保证回装方向正确；校直时加压需缓慢均匀防止过校或断裂；传动轴更换后必须重新做动平衡",
                8.0, "CK6150,CW6180,XK7132");

        addRichRule("同步带断裂风险", "同步带运行时有裂纹或缺齿可见，传动时有跳齿现象",
                "[[{\"field\":\"vibration\",\"operator\":\">\",\"value\":3.0},{\"field\":\"spindleSpeed\",\"operator\":\"<\",\"value\":300},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "同步带达到使用寿命，带齿疲劳断裂，或带轮不对中导致偏磨", "立即更换同步带，检查并调整带轮对中，确保张紧力在规定范围内",
                "传动故障", 1, 12,
                "同步带,断裂,裂纹,缺齿,张紧,带轮",
                "[\"停机后检查同步带齿面和背面\",\"用放大镜检查齿根部有无裂纹\",\"检查带轮齿槽磨损状态\",\"测量带轮平行度和对中\",\"更换同步带并按规定张力张紧\",\"手动盘车确认同步带运转平稳\",\"试运行30分钟后复检张力\"]",
                "张力计,直尺,内六角扳手,备用同步带,放大镜",
                "同步带断裂可能在加工中发生导致工件报废甚至设备损伤，发现裂纹必须立即更换；安装同步带时不得用螺丝刀撬入",
                1.5, "VMC850,XK7132,T600");

        addRichRule("传动箱润滑油变质", "传动箱油液变黑有焦味，油位过低，齿轮和轴承润滑不足",
                "[[{\"field\":\"temperature\",\"operator\":\">\",\"value\":55},{\"field\":\"vibration\",\"operator\":\">\",\"value\":2.5},{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]",
                "润滑油使用超期未更换，油品氧化变质，或传动箱密封不良导致油液泄漏", "立即更换传动箱润滑油，检查并修复漏油点，加注至标准油位，建议建立定期换油计划",
                "传动故障", 1, 25,
                "润滑油,变质,传动箱,换油,泄漏,密封",
                "[\"检查传动箱油位镜确认油位\",\"从放油口取油样检查颜色和气味\",\"检查传动箱各密封面有无渗漏\",\"完全排空旧油\",\"如油泥严重可用清洗油冲洗\",\"更换放油口密封垫片\",\"加注规定牌号新油至标准油位\",\"修复各漏油点密封\"]",
                "油液取样器,齿轮油,清洗油,密封垫片,扳手,油位镜",
                "放油前应预热设备使油液流动便于排净；热油有烫伤风险注意防护；废齿轮油按危废处理",
                2.0, "CK6150,Y3150,M1432,CW6180,TP619");
    }

    private void addRichRule(String name, String desc, String conditionJson, String cause, String action,
                             String category, int severity, int priority,
                             String keywords, String repairSteps, String toolsRequired,
                             String safetyNotes, double estimatedHours, String applicableModels) {
        DiagnosisRule rule = new DiagnosisRule();
        rule.setName(name);
        rule.setSymptomDescription(desc);
        rule.setConditionJson(conditionJson);
        rule.setPossibleCause(cause);
        rule.setRecommendedAction(action);
        rule.setFaultCategory(category);
        rule.setSeverityLevel(severity);
        rule.setPriority(priority);
        rule.setKeywords(keywords);
        rule.setRepairSteps(repairSteps);
        rule.setToolsRequired(toolsRequired);
        rule.setSafetyNotes(safetyNotes);
        rule.setEstimatedHours(estimatedHours);
        rule.setApplicableModels(applicableModels);
        diagnosisRuleRepository.save(rule);
    }
}
