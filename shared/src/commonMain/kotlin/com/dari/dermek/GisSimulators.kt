package com.dari.dermek

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private fun getSimString(key: String, lang: Language): String {
    val ru = mapOf(
        "role" to "Роль пользователя:",
        "role_farmer" to "Фермер",
        "role_inspector" to "Инспектор КВКН",
        "role_clerk" to "Кладовщик склада",
        "scan_vial" to "Сканировать QR-код флакона:",
        "select_vial" to "Выберите флакон для сканирования:",
        "vial_fmd" to "Вакцина против ящура (Серия А-102)",
        "vial_anthrax" to "Сыворотка противосибиреязвенная (Серия B-805)",
        "vial_counterfeit" to "Неизвестный флакон (Серия X-999)",
        "status" to "Статус:",
        "status_valid" to "Зарегистрирован / Активен",
        "status_violated" to "Нарушена температурная цепочка!",
        "status_counterfeit" to "Подделка / Контрафакт!",
        "temp" to "Температура при транзите:",
        "temp_opt" to "+4.2°C (Оптимальная +2°C..+8°C)",
        "temp_bad" to "+14.8°C (Превышен порог хранения!)",
        "warning_temp" to "⚠️ Внимание: Не использовать! Биологическая активность может быть снижена.",
        "warning_counterfeit" to "❌ Внимание: Препарат отсутствует в реестре ЕАЭС! Опасность конфискации.",
        "manufacturer" to "Производитель:",
        "kazbiopharm" to "РГП «Казбиофарм»",
        "biomed" to "АО «Биомед Вет»",
        "unknown" to "Неизвестен",
        "blockchain_hash" to "Хэш блокчейна QR:",
        
        "sw_upload" to "Загрузить регистрационное досье (PDF)",
        "sw_choose" to "Выберите файлы для подачи:",
        "sw_file_dossier" to "Часть 1-4 Досье CTD.pdf",
        "sw_file_gmp" to "Сертификат GMP производителя.pdf",
        "sw_missing_gmp" to "❌ Ошибка: Отсутствует обязательный сертификат GMP!",
        "sw_check_progress" to "Выполняется автоматическая проверка полноты досье...",
        "sw_check_success" to "🟢 Проверка пройдена! Досье сформировано на 100% и направлено в НРЦВ.",
        "sw_submit_btn" to "Отправить в систему",
        "sw_reset" to "Подать заново",
        
        "lis_microbiologist" to "1. Микробиолог (Стерильность и штаммы)",
        "lis_chemist" to "2. Химик-аналитик (Концентрация действующих веществ)",
        "lis_clinical" to "3. Клинический исследователь (Безопасность на животных)",
        "lis_sign" to "Подписать ЭЦП",
        "lis_signed" to "Подписано ЭЦП",
        "lis_director" to "Сформировать экспертное заключение",
        "lis_done" to "🟢 Заключение успешно сформировано, подписано ЭЦП руководства и отправлено в Комитет Ветконтроля!",
        "lis_pending" to "Ожидание подписей специалистов...",
        
        "customs_title" to "Панель таможенного контроля и балансировки объемов",
        "customs_declared" to "Заявленный объем на границе (доз):",
        "customs_received" to "Фактически принято на склад (доз):",
        "customs_balance" to "Баланс объемов партии:",
        "customs_ok" to "🟢 Объемы сбалансированы. Расхождений нет.",
        "customs_err" to "⚠️ Внимание: Обнаружено расхождение объемов! Возможное вытеснение на серый рынок.",
        "customs_discrepancy" to "Расхождение: {diff} доз ({pct}%)",
        
        "cp_title" to "Контрольный закуп и денатурация бракованных партий",
        "cp_init" to "Инициировать контрольный закуп",
        "cp_testing" to "Проводится слепой анализ качества пробы в лаборатории НРЦВ...",
        "cp_compliant" to "🟢 Compliant: Проба соответствует стандартам качества.",
        "cp_noncompliant" to "🔴 Non-Compliant: API ниже допустимого (84% вместо 98%). Серия бракована!",
        "cp_freeze" to "Заблокировать серию в ГИС",
        "cp_frozen" to "🔒 Серия заблокирована во всех аптеках и базах данных!",
        "cp_denature" to "Провести денатурацию партии",
        "cp_denatured" to "🔥 Партия химически денатурирована и уничтожена. Акт подписан Комиссией."
    )
    val kk = mapOf(
        "role" to "Пайдаланушы рөлі:",
        "role_farmer" to "Фермер",
        "role_inspector" to "КВКН инспекторы",
        "role_clerk" to "Қойма меңгерушісі",
        "scan_vial" to "Құтының QR кодын сканерлеу:",
        "select_vial" to "Сканерлеу үшін құтыны таңдаңыз:",
        "vial_fmd" to "Аусылға қарсы вакцина (А-102 сериясы)",
        "vial_anthrax" to "Сібір жарасына қарсы сарысу (B-805 сериясы)",
        "vial_counterfeit" to "Беймәлім құты (X-999 сериясы)",
        "status" to "Мәртебесі:",
        "status_valid" to "Тіркелген / Белсенді",
        "status_violated" to "Температуралық тізбек бұзылған!",
        "status_counterfeit" to "Жалған / Контрафакт!",
        "temp" to "Тасымалдау кезіндегі температура:",
        "temp_opt" to "+4.2°C (Оңтайлы +2°C..+8°C)",
        "temp_bad" to "+14.8°C (Сақтау шегінен асып кетті!)",
        "warning_temp" to "⚠️ Назар аударыңыз: Қолданбаңыз! Биологиялық белсенділігі төмендеуі мүмкін.",
        "warning_counterfeit" to "❌ Назар аударыңыз: Препарат ЕАЭО тізілімінде жоқ! Тәркілеу қаупі бар.",
        "warning_counterfeit" to "❌ Назар аударыңыз: Препарат ЕАЭО тізілімінде жоқ! Тәркілеу қаупі бар.",
        "manufacturer" to "Өндіруші:",
        "kazbiopharm" to "«Казбиофарм» РМК",
        "biomed" to "«Биомед Вет» АҚ",
        "unknown" to "Беймәлім",
        "blockchain_hash" to "QR блокчейн хэші:",
        
        "sw_upload" to "Тіркеу деректерін жүктеу (PDF)",
        "sw_choose" to "Тапсыру үшін файлдарды таңдаңыз:",
        "sw_file_dossier" to "CTD досьесінің 1-4 бөлімі.pdf",
        "sw_file_gmp" to "Өндірушінің GMP сертификаты.pdf",
        "sw_missing_gmp" to "❌ Қате: Міндетті GMP сертификаты жоқ!",
        "sw_check_progress" to "Досьенің толықтығын автоматты түрде тексеру орындалуда...",
        "sw_check_success" to "🟢 Тексеру сәтті өтті! Досье 100% жасақталып, ҰРЦВ-ға жіберілді.",
        "sw_submit_btn" to "Жүйеге жіберу",
        "sw_reset" to "Қайта тапсыру",
        
        "lis_microbiologist" to "1. Микробиолог (Стерильділік және штаммдар)",
        "lis_chemist" to "2. Химик-аналитик (Белсенді заттардың концентрациясы)",
        "lis_clinical" to "3. Клиникалық зерттеуші (Жануарлардағы қауіпсіздік)",
        "lis_sign" to "ЭЦП қол қою",
        "lis_signed" to "ЭЦП қойылды",
        "lis_director" to "Сараптамалық қорытынды жасау",
        "lis_done" to "🟢 Қорытынды сәтті жасалып, басшылықтың ЭЦП-сымен қойылып, Ветбақылау комитетіне жіберілді!",
        "lis_pending" to "Мамандардың қол қоюын күту...",
        
        "customs_title" to "Таможнялық бақылау және көлемдерді теңгерімдеу панелі",
        "customs_declared" to "Шекарада мәлімделген көлем (доза):",
        "customs_received" to "Қоймаға нақты қабылданғаны (доза):",
        "customs_balance" to "Партия көлемінің теңгерімі:",
        "customs_ok" to "🟢 Көлемдер теңгерілді. Ауытқулар жоқ.",
        "customs_err" to "⚠️ Назар аударыңыз: Көлемдердің ауытқуы анықталды! Сұр нарыққа кетуі мүмкін.",
        "customs_discrepancy" to "Ауытқу: {diff} доза ({pct}%)",
        
        "cp_title" to "Бақылау сатып алу және жарамсыз партияларды денатурациялау",
        "cp_init" to "Бақылау сатып алуды бастау",
        "cp_testing" to "ҰРЦВ зертханасында сынаманың сапасына жасырын талдау жүргізілуде...",
        "cp_compliant" to "🟢 Compliant: Сынама сапа стандарттарына сәйкес келеді.",
        "cp_noncompliant" to "🔴 Non-Compliant: API рұқсат етілгеннен төмен (98% орнына 84%). Партия жарамсыз!",
        "cp_freeze" to "Партияны ГИС-те бұғаттау",
        "cp_frozen" to "🔒 Серия барлық дәріханалар мен деректер қорында бұғатталды!",
        "cp_denature" to "Партияны денатурациялау",
        "cp_denatured" to "🔥 Партия химиялық жолмен денатурацияланды және жойылды. Акт жасалды."
    )
    return if (lang == Language.RU) ru[key] ?: key else kk[key] ?: key
}

@Composable
fun GisSystemSimulator(
    itemKey: String,
    lang: Language
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        Text(
            text = if (lang == Language.RU) "Интерактивный симулятор ГИС" else "Интерактивті ГИС симуляторы",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when (itemKey) {
            "gis_single_window" -> GisSingleWindowSimulator(lang)
            "gis_lis_workflow" -> GisLisWorkflowSimulator(lang)
            "gis_traceability_qr" -> GisTraceabilityQrSimulator(lang)
            "gis_customs_control" -> GisCustomsControlSimulator(lang)
            "gis_control_purchase" -> GisControlPurchaseSimulator(lang)
            else -> {
                Text(
                    text = if (lang == Language.RU) "Выберите цифровую услугу для запуска симулятора." else "Симуляторды іске қосу үшін цифрлық қызметті таңдаңыз.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun GisSingleWindowSimulator(lang: Language) {
    var hasDossier by remember { mutableStateOf(false) }
    var hasGmp by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var checkFinished by remember { mutableStateOf(false) }
    var checkError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(getSimString("sw_choose", lang), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // File select rows
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!isChecking && !checkFinished) hasDossier = !hasDossier
                }.padding(vertical = 6.dp)
            ) {
                Checkbox(checked = hasDossier, onCheckedChange = { if (!isChecking && !checkFinished) hasDossier = it })
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Share, contentDescription = "PDF", tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(getSimString("sw_file_dossier", lang), fontSize = 13.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!isChecking && !checkFinished) hasGmp = !hasGmp
                }.padding(vertical = 6.dp)
            ) {
                Checkbox(checked = hasGmp, onCheckedChange = { if (!isChecking && !checkFinished) hasGmp = it })
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "GMP", tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(getSimString("sw_file_gmp", lang), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isChecking) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(getSimString("sw_check_progress", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }

            if (checkFinished) {
                if (checkError) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            getSimString("sw_missing_gmp", lang),
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            getSimString("sw_check_success", lang),
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF2E7D32),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (checkFinished) {
                    Button(onClick = {
                        checkFinished = false
                        checkError = false
                        hasDossier = false
                        hasGmp = false
                    }) {
                        Text(getSimString("sw_reset", lang))
                    }
                } else {
                    Button(
                        onClick = {
                            if (hasDossier) {
                                isChecking = true
                                coroutineScope.launch {
                                    delay(1500)
                                    isChecking = false
                                    checkFinished = true
                                    checkError = !hasGmp
                                }
                            }
                        },
                        enabled = hasDossier && !isChecking
                    ) {
                        Text(getSimString("sw_submit_btn", lang))
                    }
                }
            }
        }
    }
}

@Composable
fun GisLisWorkflowSimulator(lang: Language) {
    var signedMicro by remember { mutableStateOf(false) }
    var signedChemist by remember { mutableStateOf(false) }
    var signedClinical by remember { mutableStateOf(false) }
    var signedDirector by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Lab specialist row 1
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(getSimString("lis_microbiologist", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Камера стерильности, паспорт штамма B-5", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Button(
                    onClick = { signedMicro = true },
                    enabled = !signedMicro,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (signedMicro) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (signedMicro) getSimString("lis_signed", lang) else getSimString("lis_sign", lang), fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            // Lab specialist row 2
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(getSimString("lis_chemist", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("ВЭЖХ анализатор, API: 98.4%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Button(
                    onClick = { signedChemist = true },
                    enabled = !signedChemist,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (signedChemist) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (signedChemist) getSimString("lis_signed", lang) else getSimString("lis_sign", lang), fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            // Lab specialist row 3
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(getSimString("lis_clinical", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Исследования острой токсичности на кроликах", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Button(
                    onClick = { signedClinical = true },
                    enabled = !signedClinical,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (signedClinical) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (signedClinical) getSimString("lis_signed", lang) else getSimString("lis_sign", lang), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val allSigned = signedMicro && signedChemist && signedClinical

            if (signedDirector) {
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        getSimString("lis_done", lang),
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF2E7D32),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (allSigned) "Все ЭЦП собраны!" else getSimString("lis_pending", lang),
                        fontSize = 12.sp,
                        color = if (allSigned) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Button(
                        onClick = { signedDirector = true },
                        enabled = allSigned
                    ) {
                        Text(getSimString("lis_director", lang), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GisTraceabilityQrSimulator(lang: Language) {
    var selectedRole by remember { mutableStateOf(0) } // 0: Farmer, 1: Inspector, 2: Clerk
    var scannedVial by remember { mutableStateOf(0) } // 0: None, 1: FMD, 2: Anthrax, 3: Counterfeit

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Role switcher
            Text(getSimString("role", lang), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedRole == 0,
                    onClick = { selectedRole = 0; scannedVial = 0 },
                    label = { Text(getSimString("role_farmer", lang), fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedRole == 1,
                    onClick = { selectedRole = 1; scannedVial = 0 },
                    label = { Text(getSimString("role_inspector", lang), fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedRole == 2,
                    onClick = { selectedRole = 2; scannedVial = 0 },
                    label = { Text(getSimString("role_clerk", lang), fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phone Mockup container
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Simulated Screen Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dermek QR Scanner", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        Box(
                            modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2E7D32))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(getSimString("select_vial", lang), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { scannedVial = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(getSimString("vial_fmd", lang), fontSize = 11.sp)
                        }
                        Button(
                            onClick = { scannedVial = 2 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(getSimString("vial_anthrax", lang), fontSize = 11.sp)
                        }
                        Button(
                            onClick = { scannedVial = 3 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(getSimString("vial_counterfeit", lang), fontSize = 11.sp)
                        }
                    }

                    if (scannedVial > 0) {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // QR Details Screen
                        val isFmd = scannedVial == 1
                        val isAnthrax = scannedVial == 2
                        val isFake = scannedVial == 3

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${getSimString("status", lang)} ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            val statusColor = if (isFmd) Color(0xFF2E7D32) else if (isAnthrax) Color(0xFFEF6C00) else Color(0xFFC62828)
                            val statusText = if (isFmd) getSimString("status_valid", lang) else if (isAnthrax) getSimString("status_violated", lang) else getSimString("status_counterfeit", lang)

                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(statusColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isFake) {
                            Text(getSimString("warning_counterfeit", lang), color = Color(0xFFC62828), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("${getSimString("manufacturer", lang)} ${if (isFmd) getSimString("kazbiopharm", lang) else getSimString("biomed", lang)}", fontSize = 11.sp)
                            Text("${getSimString("temp", lang)} ${if (isFmd) getSimString("temp_opt", lang) else getSimString("temp_bad", lang)}", fontSize = 11.sp)
                            if (isAnthrax) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(getSimString("warning_temp", lang), color = Color(0xFFEF6C00), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${getSimString("blockchain_hash", lang)} ${if (isFmd) "0x7a8d..f291" else "0x49c1..9a8e"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GisCustomsControlSimulator(lang: Language) {
    var borderVolume by remember { mutableStateOf(5000f) }
    var warehouseVolume by remember { mutableStateOf(5000f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(getSimString("customs_title", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Border slider
            Text("${getSimString("customs_declared", lang)} ${borderVolume.toInt()}", fontSize = 12.sp)
            Slider(
                value = borderVolume,
                onValueChange = { borderVolume = it },
                valueRange = 1000f..10000f,
                steps = 18
            )

            // Warehouse slider
            Text("${getSimString("customs_received", lang)} ${warehouseVolume.toInt()}", fontSize = 12.sp)
            Slider(
                value = warehouseVolume,
                onValueChange = { warehouseVolume = it },
                valueRange = 1000f..10000f,
                steps = 18
            )

            Spacer(modifier = Modifier.height(16.dp))

            val difference = (borderVolume - warehouseVolume).toInt()
            val differencePct = (difference.toFloat() / borderVolume.toFloat() * 100).toInt()
            val hasImbalance = abs(differencePct) > 2

            Text(getSimString("customs_balance", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (!hasImbalance) {
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        getSimString("customs_ok", lang),
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Surface(
                    color = Color(0xFFC62828).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFC62828).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            getSimString("customs_err", lang),
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            getSimString("customs_discrepancy", lang)
                                .replace("{diff}", abs(difference).toString())
                                .replace("{pct}", abs(differencePct).toString()),
                            color = Color(0xFFC62828),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GisControlPurchaseSimulator(lang: Language) {
    var testState by remember { mutableStateOf(0) } // 0: Idle, 1: Testing, 2: Compliant, 3: Non-Compliant
    var isFrozen by remember { mutableStateOf(false) }
    var isDenatured by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(getSimString("cp_title", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (testState == 0) {
                Button(
                    onClick = {
                        testState = 1
                        coroutineScope.launch {
                            delay(1800)
                            testState = if (kotlin.random.Random.nextBoolean()) 2 else 3
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(getSimString("cp_init", lang))
                }
            } else if (testState == 1) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(getSimString("cp_testing", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            } else {
                // Testing finished
                val isOk = testState == 2
                Surface(
                    color = if (isOk) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFC62828).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if (isOk) Color(0xFF2E7D32).copy(alpha = 0.3f) else Color(0xFFC62828).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        if (isOk) getSimString("cp_compliant", lang) else getSimString("cp_noncompliant", lang),
                        modifier = Modifier.padding(12.dp),
                        color = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isOk) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { isFrozen = true },
                            enabled = !isFrozen,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00), contentColor = Color.White)
                        ) {
                            Text(if (isFrozen) getSimString("cp_frozen", lang) else getSimString("cp_freeze", lang), fontSize = 12.sp)
                        }

                        Button(
                            onClick = { isDenatured = true },
                            enabled = isFrozen && !isDenatured,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828), contentColor = Color.White)
                        ) {
                            Text(if (isDenatured) getSimString("cp_denatured", lang) else getSimString("cp_denature", lang), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        testState = 0
                        isFrozen = false
                        isDenatured = false
                    }) {
                        Text(if (lang == Language.RU) "Сбросить симуляцию" else "Симуляцияны нөлдеу")
                    }
                }
            }
        }
    }
}
