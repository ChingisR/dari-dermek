package com.dari.dermek

enum class Language {
    RU, KK
}

object Localization {
    private val ruStrings = mapOf(
        "app_title" to "База знаний «Дари-дермек»",
        "running_on" to "Запущено на: ",
        "search_hint" to "Поиск нормативов и систем...",
        "all" to "Все",
        "category" to "Категория: ",
        "key_symbol" to "Символ кода: ",
        "language_toggle" to "Қазақ тілі",
        "no_items" to "Ничего не найдено",
        "category_regulations" to "Регламенты ЕАЭС",
        "category_kz" to "Национальные правила РК",
        "category_safety" to "Безопасность и списание",
        "category_systems" to "ИТ-системы",
        "category_digital" to "Цифровые услуги (ГИС)",
        "sync_source" to "Источник данных: ",
        "sync_now" to "Обновить",
        "compliance_assessment" to "Интерактивная оценка соответствия",
        "compliance_status" to "Статус: ",
        "compliant" to "Полное соответствие",
        "partially_compliant" to "Частичное соответствие",
        "non_compliant" to "Не соответствует",
        "no_checklist" to "Для этой категории чек-лист не предусмотрен",
        
        // Calculator & Simulator Translations
        "timeline_calc" to "Калькулятор сроков регистрации ЕАЭС",
        "drug_type" to "Тип ветеринарного препарата:",
        "pharmaceutical" to "Фармацевтический",
        "biological" to "Иммунологический / Биологический",
        "active_substance_status" to "Действующее вещество:",
        "substance_new" to "Новое вещество",
        "substance_listed" to "Входит в перечни (Прил. 8/16.1)",
        "calculated_timeline" to "Расчетный срок экспертизы:",
        "working_days" to "рабочих дней",
        "required_docs" to "Обязательный пакет документов (ЕАЭС CTD):",
        "required_trials" to "Материалы для экспертизы качества (испытаний):",
        "no_trials_needed" to "Лабораторные испытания образцов не требуются",
        "sim_workflow" to "Симулятор цифрового процесса (ГИС)",
        "sim_submit" to "Подать заявление",
        "sim_upload" to "Загрузить досье",
        "sim_samples" to "Сдать образцы",
        "sim_test" to "Запустить тесты",
        "sim_approve" to "Утвердить регистрацию",
        "sim_restart" to "Сбросить симуляцию",
        "sim_state_idle" to "Начало симуляции. Ожидание подачи заявления через eGov SSO.",
        "sim_state_submitted" to "Шаг 1: Заявление подано. ГИС проверяет полноту документов.",
        "sim_state_dossier" to "Шаг 2: Досье и НТД загружены в защищенный цифровой сейф.",
        "sim_state_samples" to "Шаг 3: Образцы переданы в НРЦВ. Запущен таймер доставки (45 дней).",
        "sim_state_testing" to "Шаг 4: Лаборатории НРЦВ (микробиологи, химики, клиницисты) проводят исследования в ЛИС.",
        "sim_state_approved" to "Шаг 5: Протокол подписан ЭЦП. Комитет КВКН внес препарат в Единый реестр ЕАЭС!"
    )

    private val kkStrings = mapOf(
        "app_title" to "«Дари-дермек» білім қоры",
        "running_on" to "Жүйеде жұмыс істейді: ",
        "search_hint" to "Нормативтер мен жүйелерді іздеу...",
        "all" to "Барлығы",
        "category" to "Санат: ",
        "key_symbol" to "Код белгісі: ",
        "language_toggle" to "Русский язык",
        "no_items" to "Ештеңе табылмады",
        "category_regulations" to "ЕАЭО регламенттері",
        "category_kz" to "ҚР ұлттық ережелері",
        "category_safety" to "Қауіпсіздік және есептен шығару",
        "category_systems" to "АТ-жүйелері",
        "category_digital" to "Цифрлық қызметтер (ГИС)",
        "sync_source" to "Деректер көзі: ",
        "sync_now" to "Жаңарту",
        "compliance_assessment" to "Сәйкестікті интерактивті бағалау",
        "compliance_status" to "Мәртебесі: ",
        "compliant" to "Толық сәйкестік",
        "partially_compliant" to "Жартылай сәйкестік",
        "non_compliant" to "Сәйкес емес",
        "no_checklist" to "Бұл санат үшін чек-парақ қарастырылмаған",
        
        // Calculator & Simulator Translations
        "timeline_calc" to "ЕАЭО тіркеу мерзімін есептеу калькуляторы",
        "drug_type" to "Ветеринариялық препараттың түрі:",
        "pharmaceutical" to "Фармацевтикалық",
        "biological" to "Иммунологиялық / Биологиялық",
        "active_substance_status" to "Әсер етуші зат:",
        "substance_new" to "Жаңа зат",
        "substance_listed" to "Тізілімдерге кіреді (Қосымша 8/16.1)",
        "calculated_timeline" to "Сараптаманың есептік мерзімі:",
        "working_days" to "жұмыс күні",
        "required_docs" to "Міндетті құжаттар пакеті (ЕАЭО CTD):",
        "required_trials" to "Сапа сараптамасына арналған материалдар:",
        "no_trials_needed" to "Үлгілерді зертханалық сынау талап етілмейді",
        "sim_workflow" to "Сандық процесс симуляторы (ГИС)",
        "sim_submit" to "Өтініш беру",
        "sim_upload" to "Досьені жүктеу",
        "sim_samples" to "Үлгілерді тапсыру",
        "sim_test" to "Сынақтарды бастау",
        "sim_approve" to "Тіркеуді бекіту",
        "sim_restart" to "Симуляцияны ысыру",
        "sim_state_idle" to "Симуляцияның басталуы. eGov SSO арқылы өтініш беруді күту.",
        "sim_state_submitted" to "1-қадам: Өтініш берілді. ГИС құжаттардың толықтығын тексереді.",
        "sim_state_dossier" to "2-қадам: Тіркеу деректері мен НТҚ қорғалған сандық сейфке жүктелді.",
        "sim_state_samples" to "3-қадам: Үлгілер НРЦВ-ға тапсырылды. Жеткізу таймері іске қосылды (45 күн).",
        "sim_state_testing" to "4-қадам: НРЦВ зертханалары (микробиологтар, химиктер, клиницистер) ЛАЖ (LIS) ішінде зерттеу жүргізуде.",
        "sim_state_approved" to "5-қадам: Хаттамаға ЭЦП-мен қол қойылды. КВКН комитеті препаратты Бірыңғай ЕАЭО тізіліме енгізді!"
    )

    fun getString(key: String, lang: Language): String {
        return when (lang) {
            Language.RU -> ruStrings[key] ?: key
            Language.KK -> kkStrings[key] ?: key
        }
    }
}
