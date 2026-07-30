package com.dari.dermek

import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.get
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RegulationRepository {
    private val settings: com.russhwolf.settings.Settings = com.russhwolf.settings.Settings()
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val cacheKey = "cached_regulations"

    private val seedData = listOf(
        RegulationItem(
            title = "vet_drugs_eaeu",
            descriptionEn = "EAEU Vet Medicines Rules",
            descriptionRu = "Правила обращения ветпрепаратов ЕАЭС",
            descriptionKk = "ЕАЭО мал дәрі-дәрмектерінің айналымы ережелері",
            category = "Regulations",
            detailsEn = "Unified requirements for veterinary medicine circulation across EAEU member states. Covers pre-clinical trials, GMP manufacturing standards, registration dossiers, and post-authorization pharmacovigilance. Transition period ends on December 31, 2030.",
            detailsRu = "Единые требования к обращению ветеринарных лекарственных средств на территории ЕАЭС. Включают доклинические испытания, стандарты производства GMP, регистрационные досье и фармаконадзор. Переходный период завершается 31 декабря 2030 года.",
            detailsKk = "ЕАЭО аумағындағы ветеринариялық дәрілік заттардың айналымына қойылатын бірыңғай талаптар. Клиникаға дейінгі сынақтарды, GMP өндірістік стандарттарын, тіркеу деректерін және фармакологиялық бақылауды қамтиды. Өтпелі кезең 2030 жылғы 31 желтоқсанда аяқталады.",
            checklistRu = listOf(
                "Доклинические исследования выполнены согласно ГОСТ",
                "Соблюдены стандарты производства GMP",
                "Регистрационное досье подготовлено по формату ЕАЭС",
                "Сформирован ежегодный отчет фармаконадзора"
            ),
            checklistKk = listOf(
                "Клиникаға дейінгі зерттеулер МЕМСТ бойынша орындалды",
                "GMP өндірістік стандарттары сақталды",
                "Тіркеу деректері ЕАЭО форматында дайындалды",
                "Жыл сайынғы фармакологиялық бақылау есебі қалыптастырылды"
            )
        ),
        RegulationItem(
            title = "diagnostics_eaeu",
            descriptionEn = "EAEU Veterinary Diagnostics Rules",
            descriptionRu = "Правила обращения диагностических средств ЕАЭС",
            descriptionKk = "ЕАЭО диагностикалық құралдар айналымының ережелері",
            category = "Regulations",
            detailsEn = "Governs in vitro diagnostic kits, reagents, and test sets. Establishes two classes based on epidemiological risk: Category 1 (WOAH listed diseases) requiring physical sample lab testing, and Category 2 (others) undergoing paper-only evaluation. Transition period ends December 31, 2032.",
            detailsRu = "Регулирует обращение диагностических наборов in vitro и реагентов. Устанавливает два класса по риску: Категория 1 (болезни МЭБ/WOAH), требующая лабораторных испытаний образцов, и Категория 2 (прочие), проходящая бумажную оценку. Срок перехода — до 31 декабря 2032 года.",
            detailsKk = "In vitro диагностикалық жинақтар мен реагенттердің айналымын реттейді. Тәуекел бойынша екі класты белгілейді: Үлгілерді зертханалық сынауды талап ететін 1-санат (ХЭБ/WOAH аурулары) және құжаттамалық бағалаудан өтетін 2-санат (басқалары). Өту мерзімі — 2032 жылғы 31 желтоқсанға дейін.",
            checklistRu = listOf(
                "Определен класс эпидемиологического риска (Кат. 1 или 2)",
                "Проведены лабораторные испытания образцов в аккредитованном центре",
                "Собраны научные отчеты и доказательства эффективности"
            ),
            checklistKk = listOf(
                "Эпидемиологиялық тәуекел класы анықталды (1 немесе 2 санат)",
                "Акредиттелген орталықта үлгілерге зертханалық сынақтар жүргізілді",
                "Тиімділікті растайтын ғылыми есептер мен дәлелдер жиналды"
            )
        ),
        RegulationItem(
            title = "disinfectants_eaeu",
            descriptionEn = "EAEU Disinfectants Rules",
            descriptionRu = "Правила обращения дезинфицирующих средств ЕАЭС",
            descriptionKk = "ЕАЭО дезинфекциялық құралдардың айналымы ережелері",
            category = "Regulations",
            detailsEn = "Supranational rules regulating disinfectants, disinsectants, and deacaricides used in veterinary settings. Harmonizes registration and quality assurance protocols, with transition completed by December 31, 2032.",
            detailsRu = "Наднациональные правила обращения ветеринарных дезинфицирующих, дезинсекционных и дезакаризационных средств. Гармонизируют регистрацию и контроль качества. Срок перехода — до 31 декабря 2032 года.",
            detailsKk = "Ветеринариялық дезинфекциялық, дезинсекциялық және дезакаризациялық құралдардың айналымының наднационалдық ережелері. Тіркеуді және сапаны бақылауды үйлестіреді. Өту мерзімі — 2032 жылғы 31 желтоқсанға дейін.",
            checklistRu = listOf(
                "Проведена токсикологическая оценка безопасности",
                "Согласованы инструкции по экспозиции и дозировкам",
                "Средство внесено в общий реестр ЕАЭС"
            ),
            checklistKk = listOf(
                "Қауіпсіздікке токсикологиялық бағалау жүргізілді",
                "Қолдану уақыты мен дозалары бойынша нұсқаулықтар келісілді",
                "Құрал жалпы ЕАЭО тізіліміне енгізілді"
            )
        ),
        RegulationItem(
            title = "registration_rules_kz",
            descriptionEn = "KZ State Registration Service",
            descriptionRu = "Государственная регистрация ветпрепаратов в РК",
            descriptionKk = "ҚР-да ветеринариялық препараттарды мемлекеттік тіркеу",
            category = "KZ National",
            detailsEn = "Governs the licensing and registration of veterinary preparations and feed additives in the Republic of Kazakhstan. Certificates are issued for 5 years (primary) or permanently (repeated). Includes emergency 2-year temporary registration.",
            detailsRu = "Регулирует выдачу регистрационных удостоверений на ветеринарные препараты и кормовые добавки в РК. Выдается на 5 лет (первичная) или бессрочно (повторная). Включает экстренную временную регистрацию до 2 лет.",
            detailsKk = "ҚР-дағы ветеринариялық препараттар мен жемшөп қоспаларына тіркеу куәліктерін беруді реттейді. 5 жылға (бастапқы) немесе мерзімсіз (қайталама) беріледі. 2 жылға дейінгі шұғыл уақытша тіркеуді қамтиды.",
            checklistRu = listOf(
                "Подано заявление в Министерство сельского хозяйства РК",
                "Уплачена государственная пошлина в бюджет",
                "Получено экспертное заключение ветеринарной лаборатории",
                "Свидетельство о регистрации выдано на руки"
            ),
            checklistKk = listOf(
                "ҚР Ауыл шаруашылығы министрлігіне өтініш берілді",
                "Бюджетке мемлекеттік баж төленді",
                "Ветеринариялық зертхананың сараптамалық қорытындысы алынды",
                "Тіркеу туралы куәлік қолға берілді"
            )
        ),
        RegulationItem(
            title = "ntd_approval_kz",
            descriptionEn = "KZ NTD Approval Process",
            descriptionRu = "Согласование нормативно-технической документации РК",
            descriptionKk = "ҚР нормативтік-техникалық құжаттамасын келісу",
            category = "KZ National",
            detailsEn = "Describes the free 30-working-day state service to approve Normative-Technical Documentation (NTD) detailing physical/chemical attributes, packaging, and manufacturer specifications.",
            detailsRu = "Регулирует бесплатную государственную услугу по согласованию нормативно-технической документации (НТД) в течение 30 рабочих дней. Определяет показатели качества, рецептуры и упаковку.",
            detailsKk = "30 жұмыс күні ішінде нормативтік-техникалық құжаттаманы (НТҚ) келісу бойынша тегін мемлекеттік қызметті реттейді. Сапа көрсеткіштерін, рецептураларды және қаптаманы анықтайды.",
            checklistRu = listOf(
                "Разработан проект спецификации производителя",
                "Согласованы методы контроля качества препарата",
                "Утвержден дизайн и маркировка упаковки"
            ),
            checklistKk = listOf(
                "Өндіруші спецификациясының жобасы әзірленді",
                "Препарат сапасын бақылау әдістері келісілді",
                "Қаптаманың дизайны мен таңбалануы бекітілді"
            )
        ),
        RegulationItem(
            title = "testing_and_trials_kz",
            descriptionEn = "Approbation and Registration Trials",
            descriptionRu = "Апробация и регистрационные испытания в РК",
            descriptionKk = "ҚР-да апробациялау и тіркеу сынақтары",
            category = "KZ National",
            detailsEn = "Mandatory testing conducted at the National Reference Center for Veterinary Medicine. Approbation (new domestic drugs) or registration trials (imported drugs, up to 2 years) must be completed before state registration.",
            detailsRu = "Обязательные лабораторные и полевые испытания на базе Национального референтного центра ветеринарии. Апробация (новые отечественные) или испытания (импортные, до 2 лет) проводятся до подачи на регистрацию.",
            detailsKk = "Ұлттық ветеринариялық референттік орталық негізінде міндетті зертханалық және далалық сынақтар. Апробация (жаңа отандық) немесе сынақтар (импорттық, 2 жылға дейін) тіркеуге өтініш бергенге дейін жүргізіледі.",
            checklistRu = listOf(
                "Заключен договор на испытания с Национальным референтным центром",
                "Переданы образцы трех серий препарата с завода",
                "Получен официальный акт об успешной апробации"
            ),
            checklistKk = listOf(
                "Ұлттық референттік орталықпен сынақ жүргізуге келісімшарт жасалды",
                "Зауыттан препараттың үш сериясының үлгілері тапсырылды",
                "Сәтті апробациядан өткені туралы ресми акт алынды"
            )
        ),
        RegulationItem(
            title = "safety_monitoring_kz",
            descriptionEn = "KZ Pharmacovigilance Monitoring",
            descriptionRu = "Фармаконадзор и мониторинг безопасности в РК",
            descriptionKk = "ҚР-да фармакологиялық қадағалау және қауіпсіздікті бақылау",
            category = "Safety & Disposal",
            detailsEn = "Mandatory quarterly reports on side effects and ineffectiveness submitted by the State Veterinary Organization. KVKN holds power to mandate additional testing or revoke registration based on safety reports.",
            detailsRu = "Обязательный ежеквартальный мониторинг побочных действий. Сведения побочных явлений подаются производителями в уполномоченный орган.",
            detailsKk = "Жанама әсерлердің міндетті тоқсан сайынғы мониторингі. Өндірушілер жанама әсерлер туралы мәліметтерді уәкілетті органға ұсынады.",
            checklistRu = listOf(
                "Назначено ответственное за фармаконадзор лицо",
                "Ведется электронный журнал регистрации нежелательных реакций",
                "Отчет отправлен в КВКН МСХ РК до 30 числа последнего месяца квартала"
            ),
            checklistKk = listOf(
                "Фармакологиялық қадағалауға жауапты тұлға тағайындалды",
                "Жанама әсерлерді тіркеудің электронды журналы жүргізілуде",
                "Есеп тоқсанның соңғы айының 30-ына дейін ҚР АШМ КВКН-ге жіберілді"
            )
        ),
        RegulationItem(
            title = "disposal_and_writeoff_kz",
            descriptionEn = "Disposal and Write-off Rules",
            descriptionRu = "Правила списания и утилизации препаратов в РК",
            descriptionKk = "ҚР-да препараттарды есептен шығару және жою ережелері",
            category = "Safety & Disposal",
            detailsEn = "Procedures for the safe destruction of expired, contaminated, or laboratory-rejected veterinary medicines. Requires mandatory chemical denaturation with kerosene, petroleum, or bleach before physical disposal.",
            detailsRu = "Порядок утилизации просроченных или непригодных препаратов. В госсекторе создается комиссия (мин. 3 человека). Обязательна денатурация керосином, дегтем или хлорной известью перед уничтожением.",
            detailsKk = "Мерзімі өткен немесе жарамсыз препараттарды кәдеге жарату тәртібі. Мемлекеттік секторда комиссия құрылады (кемінде 3 адам). Жойылу алдында керосинмен, қара маймен немесе хлорлы әкпен денатурациялау міндетті.",
            checklistRu = listOf(
                "Сформирована комиссия по списанию (не менее 3-х специалистов)",
                "Проведена химическая денатурация керосином или дегтем",
                "Составлен официальный акт об утилизации"
            ),
            checklistKk = listOf(
                "Есептен шығару комиссиясы құрылды (кемінде 3 маман)",
                "Керосинмен немесе қара маймен химиялық денатурация жүргізілді",
                "Жою туралы ресми акт жасалды"
            )
        ),
        RegulationItem(
            title = "prohibited_drugs_kz",
            descriptionEn = "Prohibited Veterinary Substances",
            descriptionRu = "Запрещенные ветеринарные вещества в РК",
            descriptionKk = "ҚР-да тыйым салынған ветеринариялық заттар",
            category = "Safety & Disposal",
            detailsEn = "Bans anabolic steroids, antibiotic growth promoters, and medical reserve antibiotics (such as chloramphenicol and metronidazole) in food-producing animals to ensure human food safety.",
            detailsRu = "Запрещает использование гормонов роста, антибиотиков в качестве стимуляторов продуктивности и медицинских резервных антибиотиков (левомицетин, метронидазол) у продуктивных животных.",
            detailsKk = "Өнімді жануарларда өсу гормондарын, өнімділікті ынталандырушы ретіндегі антибиотиктерді және медициналық резервтік антибиотиктерді (левомицетин, метронидазол) пайдалануға тыйым салады.",
            checklistRu = listOf(
                "Подтверждено отсутствие гормонов роста в кормах",
                "Проведен лабораторный анализ сырья на левомицетин",
                "Исключено использование метронидазола в ветеринарии"
            ),
            checklistKk = listOf(
                "Жемшөпте өсу гормондарының жоқтығы расталды",
                "Шикізатқа левомицетинге зертханалық талдау жүргізілді",
                "Ветеринарияда метронидазолды қолдану алынып тасталды"
            )
        ),
        RegulationItem(
            title = "galen_component",
            descriptionEn = "Galen Component (Rosselkhoznadzor)",
            descriptionRu = "Компонент «Гален» (Россельхознадзор)",
            descriptionKk = "«Гален» компоненті (Россельхознадзор)",
            category = "Systems",
            detailsEn = "A Java-based web application within Russia's VetIS ecosystem. Tracks state registration of veterinary medicines, feed additives, and GMO substances, as well as electronic prescriptions and batch releases.",
            detailsRu = "Специализированная веб-система в составе ВетИС. Обеспечивает ведение реестров лекарственных средств, субстанций, кормовых добавок и разрешений на ввод препаратов в гражданский оборот.",
            detailsKk = "ВетИС құрамындағы мамандандырылған веб-жүйе. Дәрілік заттардың, субстанциялардың, жемшөп қоспаларының тізілімдерін және препараттарды азаматтық айналымға енгізуге рұқсаттарды жүргізуді қамтамасыз етеді.",
            checklistRu = listOf(
                "Подключен доступ к веб-порталу ВетИС",
                "Проведена интеграция по API для передачи рецептов",
                "Синхронизирован реестр разрешенных препаратов"
            ),
            checklistKk = listOf(
                "ВетИС веб-порталына кіру қосылды",
                "Рецепттерді жіберу үшін API бойынша интеграция жүргізілді",
                "Рұқсат етілген препараттардың тізілімі синхрондалды"
            )
        ),
        RegulationItem(
            title = "digital_experts_group",
            descriptionEn = "Digital Experts Group (DEG)",
            descriptionRu = "Digital Experts Group (DEG)",
            descriptionKk = "Digital Experts Group (DEG)",
            category = "Systems",
            detailsEn = "Specialized software development and consulting agency. Operators of the 'Дари-дермек' digital system. Host of the 'SciEdTech Futurity' academic school.",
            detailsRu = "Казахстанская ИТ и консалтинговая компания. Разработчик проекта «Дари-дермек». Курирует научную школу SciEdTech Futurity и реестры сертификатов повышения квалификации.",
            detailsKk = "Қазақстандық АТ және консалтингтік компания. «Дари-дермек» жобасын әзірлеуші. SciEdTech Futurity ғылыми мектебін және біліктілікті арттыру сертификаттарының тізілімдерін үйлестіреді.",
            checklistRu = listOf(
                "Внедрена платформа управления квалификацией",
                "Реестр SciEdTech Futurity запущен в промышленную эксплуатацию",
                "Консалтинговые линии поддержки клиентов активны"
            ),
            checklistKk = listOf(
                "Біліктілікті басқару платформасы енгізілді",
                "SciEdTech Futurity тізілімі өнеркәсіптік пайдалануға жіберілді",
                "Клиенттерді қолдаудың консалтингтік желілері белсенді"
            )
        ),
        RegulationItem(
            title = "eaeu_compliance",
            descriptionEn = "EAEU Compliance Bring-up",
            descriptionRu = "Приведение в соответствие ЕАЭС",
            descriptionKk = "ЕАЭО талаптарына сәйкестікке келтіру",
            category = "Regulations",
            detailsEn = "Procedure to align existing national veterinary registrations with EAEU supranational regulations before the Dec 31, 2030 deadline.",
            detailsRu = "Процедура приведения ранее зарегистрированных национальных препаратов в соответствие с требованиями ЕАЭС. Переходный период длится до 31 декабря 2030 года.",
            detailsKk = "Бұрын тіркелген ұлттық препараттарды ЕАЭО талаптарына сәйкестікке келтіру процедурасы. Өтпелі кезең 2030 жылғы 31 желтоқсанға дейін жалғасады.",
            isRegistrationProcedure = true,
            minTimelineDays = 70,
            maxTimelineDays = 90,
            documentChecklistRu = listOf(
                "Заявление о приведении в соответствие (форма 10.5)",
                "Обновленное регистрационное досье в формате ЕАЭС",
                "Пояснительная записка-обоснование об отсутствии критических отличий",
                "Периодический отчет по безопасности (PSUR) за последние 5 лет",
                "Документ об уплате государственной пошлины за экспертизу"
            ),
            documentChecklistKk = listOf(
                "Сәйкестікке келтіру туралы өтініш (10.5 нысаны)",
                "ЕАЭО форматындағы жаңартылған тіркеу деректері",
                "Критикалық айырмашылықтардың жоқтығы туралы түсіндірме хат",
                "Соңғы 5 жылдағы қауіпсіздік туралы мерзімді есеп (PSUR)",
                "Сараптама үшін мемлекеттік баж салығын төлеу туралы құжат"
            ),
            trialChecklistRu = listOf(
                "Стандартные образцы действующего вещества для 3-кратного анализа",
                "Специфические реагенты и расходные материалы по согласованию",
                "Тест-системы или штаммы микроорганизмов (для биологических препаратов)"
            ),
            trialChecklistKk = listOf(
                "3 еселік талдауға арналған әсер етуші заттың стандартты үлгілері",
                "Келісім бойынша ерекше реагенттер мен шығыс материалдары",
                "Микроорганизмдердің тест-жүйелері немесе штаммдары (биологиялық үшін)"
            ),
            checklistRu = listOf(
                "Подготовлено регистрационное досье по стандартам ЕАЭС",
                "Составлен 5-летний отчет по безопасности (PSUR)",
                "Подготовлены образцы для трехкратной воспроизводимости"
            ),
            checklistKk = listOf(
                "ЕАЭО стандарттары бойынша тіркеу деректері дайындалды",
                "5 жылдық қауіпсіздік есебі жасалды (PSUR)",
                "Үш еселік талдау үшін үлгілер дайындалды"
            )
        ),
        RegulationItem(
            title = "eaeu_standard_reg",
            descriptionEn = "EAEU Standard Registration",
            descriptionRu = "Стандартная регистрация ЕАЭС",
            descriptionKk = "ЕАЭО стандартты тіркеуі",
            category = "Regulations",
            detailsEn = "Full EAEU registration pathway for brand-new veterinary drugs. Involves exhaustive administrative, pharmaceutical, safety, and clinical evaluations.",
            detailsRu = "Полная процедура регистрации нового ветеринарного препарата в рамках ЕАЭС. Включает экспертизу досье и обязательные лабораторные испытания качества.",
            detailsKk = "ЕАЭО аясында жаңа ветеринариялық препаратты тіркеудің толық процедурасы. Досье сараптамасын және міндетті зертханалық сапа сынақтарын қамтиды.",
            isRegistrationProcedure = true,
            minTimelineDays = 95,
            maxTimelineDays = 100,
            documentChecklistRu = listOf(
                "Заявление на регистрацию нового препарата",
                "Лицензия на производство и сертификат GMP производителя",
                "Часть 1: Административные документы (инструкция, макеты упаковки)",
                "Часть 2: Химические, фармацевтические и биологические данные",
                "Часть 3: Токсикологические и фармакологические отчеты (для продуктивных)",
                "Часть 4: Отчеты о клинических исследованиях эффективности"
            ),
            documentChecklistKk = listOf(
                "Жаңа препаратты тіркеуге өтініш",
                "Өндірушінің өндірістік лицензиясы және GMP сертификаты",
                "1-бөлім: Әкімшілік құжаттар (нұсқаулық, қаптама макеттері)",
                "2-бөлім: Химиялық, фармацевтикалық және биологиялық деректер",
                "3-бөлім: Токсикологиялық және фармакологиялық есептер",
                "4-бөлім: Клиникалық тиімділікті зерттеу есептері"
            ),
            trialChecklistRu = listOf(
                "Образцы препарата из трех промышленных серий",
                "Стандартные образцы активных субстанций",
                "Методы контроля качества и валидационные отчеты методик"
            ),
            trialChecklistKk = listOf(
                "Үш өнеркәсіптік сериядан алынған препарат үлгілері",
                "Белсенді субстанциялардың стандартты үлгілері",
                "Сапаны бақылау әдістері және әдістемелерді валидациялау есептері"
            ),
            checklistRu = listOf(
                "Заполнены все 4 части ЕАЭС досье",
                "Предоставлены образцы трех серий производства",
                "Методы контроля качества валидированы"
            ),
            checklistKk = listOf(
                "ЕАЭО тіркеу деректерінің барлық 4 бөлімі толтырылды",
                "Өндірістің үш сериясының үлгілері ұсынылды",
                "Сапаны бақылау әдістері валидацияланды"
            )
        ),
        RegulationItem(
            title = "eaeu_simplified_reg",
            descriptionEn = "EAEU Simplified Registration",
            descriptionRu = "Упрощенная регистрация ЕАЭС",
            descriptionKk = "ЕАЭО жеңілдетілген тіркеуі",
            category = "Regulations",
            detailsEn = "Accelerated registration procedure for generic (reproduced) veterinary medicines upon proving equivalence to the reference drug.",
            detailsRu = "Регистрация воспроизведенных ветеринарных препаратов (дженериков) по упрощенной процедуре при подтверждении их биоэквивалентности референтному препарату.",
            detailsKk = "Биоэквиваленттілігі референтті препаратқа расталған жағдайда қайта өндірілген ветеринариялық препараттарды (генериктерді) жеңілдетілген процедура бойынша тіркеу.",
            isRegistrationProcedure = true,
            minTimelineDays = 35,
            maxTimelineDays = 45,
            documentChecklistRu = listOf(
                "Заявление на регистрацию воспроизведенного препарата",
                "Документы о регистрации и деталях референтного препарата в ЕАЭС",
                "Отчет об исследованиях биоэквивалентности или терапевтической эквивалентности",
                "Лицензия и GMP сертификат завода-изготовителя"
            ),
            documentChecklistKk = listOf(
                "Қайта өндірілген препаратты тіркеуге өтініш",
                "ЕАЭО-дағы референтті препаратты тіркеу орындары",
                "Биоэквиваленттілікті немесе терапиялық баламалылықты зерттеу есебі",
                "Өндіруші зауыттың лицензиясы мен GMP сертификаты"
            ),
            trialChecklistRu = listOf(
                "Образцы генерика для проведения экспертизы качества",
                "Стандартные образцы действующего вещества"
            ),
            trialChecklistKk = listOf(
                "Сапа сараптамасын жүргізуге арналған генерик үлгілері",
                "Әсер етуші заттың стандартты үлгілері"
            ),
            checklistRu = listOf(
                "Подтверждена биоэквивалентность референтному средству",
                "Образцы генерика переданы на экспертизу качества"
            ),
            checklistKk = listOf(
                "Референтті құралға биоэквиваленттілігі расталды",
                "Генерик үлгілері сапа сараптамасына тапсырылды"
            )
        ),
        RegulationItem(
            title = "eaeu_recognition",
            descriptionEn = "EAEU Mutual Recognition",
            descriptionRu = "Процедура признания ЕАЭС",
            descriptionKk = "ЕАЭО өзара тану процедурасы",
            category = "Regulations",
            detailsEn = "Fast-track administrative path to authorize a drug in a concerned state based on prior registration in an EAEU reference state.",
            detailsRu = "Признание регистрации ветеринарного препарата в новом государстве-члене на основании экспертизы досье и решений референтного органа без новых лаб испытаний.",
            detailsKk = "Жаңа зертханалық сынақтарсыз тіркеу деректерін және референтті органның шешімдерін сараптау негізінде жаңа мүше мемлекетте ветеринариялық препараттың тіркелуін тану.",
            isRegistrationProcedure = true,
            minTimelineDays = 45,
            maxTimelineDays = 45,
            documentChecklistRu = listOf(
                "Заявление о признании регистрации в новом государстве-члене",
                "Копия экспертного заключения референтного государства",
                "Действующее регистрационное удостоверение референтного государства",
                "Полный пакет утвержденного досье (включая инструкцию и спецификации)"
            ),
            documentChecklistKk = listOf(
                "Жаңа мүше мемлекетте тіркеуді мойындау туралы өтініш",
                "Референттік мемлекеттің сараптамалық қорытындысының көшірмесі",
                "Референттік мемлекеттің қолданыстағы тіркеу куәлігі",
                "Бекітілген деректердің толық пакеті (нұсқаулық пен спецификацияны қоса)"
            ),
            trialChecklistRu = emptyList(),
            trialChecklistKk = emptyList(),
            checklistRu = listOf(
                "Препарат успешно зарегистрирован в референтной стране ЕАЭС",
                "Референтное экспертное заключение переведено на русский язык",
                "Подана заявка на взаимное признание через электронный портал"
            ),
            checklistKk = listOf(
                "Препарат ЕАЭО референтті елінде сәтті тіркелді",
                "Референттік сараптамалық қорытынды орыс тіліне аударылды",
                "Электрондық портал арқылы өзара тануға өтінім берілді"
            )
        ),
        RegulationItem(
            title = "gis_single_window",
            descriptionEn = "Single Window & eGov Integration",
            descriptionRu = "Единое окно и интеграция eGov",
            descriptionKk = "Бірыңғай терезе және eGov интеграциясы",
            category = "Systems",
            detailsEn = "Re-engineered portal unifying former separate approbation and registration tasks into a single electronic service backed by eGov SSO.",
            detailsRu = "Единая точка входа для заявителей через eGov SSO. Ликвидирует раздельные бумажные процессы, переводя весь поток документов в облачную ГИС.",
            detailsKk = "eGov SSO арқылы өтініш берушілерге арналған бірыңғай кіру нүктесі. Барлық құжат ағынын бұлтты ГИС-ке аудара отырып, жеке қағаз процестерін жояды.",
            checklistRu = listOf(
                "Реализована авторизация eGov SSO и ЭЦП-подписание",
                "Объединены заявки на испытания и госрегистрацию",
                "Создан защищенный кабинет разработчика препарата"
            ),
            checklistKk = listOf(
                "eGov SSO авторизациясы және ЭЦП-қол қою жүзеге асырылды",
                "Сынақтар мен мемлекеттік тіркеуге өтінімдер біріктірілді",
                "Препаратты әзірлеушінің қорғалған кабинеті құрылды"
            )
        ),
        RegulationItem(
            title = "gis_lis_workflow",
            descriptionEn = "LIS Digital Laboratory Workflow",
            descriptionRu = "Цифровая лаборатория и ЛИС",
            descriptionKk = "Сандық зертхана және ЛАЖ (LIS)",
            category = "Systems",
            detailsEn = "Integrated LIS at NRCV coordinates microbiologists, chemical analysts, and clinical teams to draft and sign protocols digitally.",
            detailsRu = "Лабораторная информационная система НРЦВ. Маршрутизирует задачи экспертам и позволяет подписывать протоколы ЭЦП без бумажного документооборота.",
            detailsKk = "НРЦВ зертханалық ақпараттық жүйесі. Сарапшыларға тапсырмаларды бағыттайды және қағаз құжат айналымынсыз ЭЦП хаттамаларына қол қоюға мүмкіндік береді.",
            checklistRu = listOf(
                "Настроена маршрутизация по отделам (микробиология, химия, клиника)",
                "Внедрено совместное редактирование и ЭЦП подписание протоколов",
                "Добавлена возможность выездных аудитов лабораторий завода"
            ),
            checklistKk = listOf(
                "Бөлімдер бойынша бағыттау реттелді (микробиология, химия, клиника)",
                "Хаттамаларды бірлесіп редакциялау және ЭЦП-қол қою енгізілді",
                "Зауыт зертханаларына көшпелі аудит жүргізу мүмкіндігі қосылды"
            )
        ),
        RegulationItem(
            title = "gis_traceability_qr",
            descriptionEn = "Traceability & Vial QR Codes",
            descriptionRu = "Прослеживаемость и QR-коды",
            descriptionKk = "Қадағалану және QR-кодтар",
            category = "Systems",
            detailsEn = "Traceability engine using serial QR codes on vaccine/drug vials. Integrated with a mobile app for inspectors and veterinarians to check registrations.",
            detailsRu = "Мониторинг движения партий по индивидуальным QR-кодам на флаконах. Сканирование мобильным приложением подтверждает легальность и соблюдение температурной цепи.",
            detailsKk = "Құтылардағы жеке QR-кодтар бойынша партиялардың қозғалысын бақылау. Мобильді қосымшамен сканерлеу заңдылықты және температуралық тізбектің сақталуын растайды.",
            checklistRu = listOf(
                "Разработана система генерации уникальных QR-кодов на флаконы",
                "Создано мобильное приложение для инспекторов и ветеринаров",
                "Интегрированы датчики контроля температурного режима"
            ),
            checklistKk = listOf(
                "Құтыларға бірегей QR-кодтарды генерациялау жүйесі әзірленді",
                "Инспекторлар мен ветеринарларға арналған мобильді қосымша құрылды",
                "Температуралық режимді бақылау датчиктері біріктірілді"
            )
        ),
        RegulationItem(
            title = "gis_customs_control",
            descriptionEn = "Border & Customs Integration",
            descriptionRu = "Импортный контроль и граница",
            descriptionKk = "Импорттық бақылау және шекара",
            category = "Systems",
            detailsEn = "API link with customs authorities at EAEU entry points. Holds unregistered pharmaceutical batches and matches imported volumes with warehouse receipts.",
            detailsRu = "Интеграция с таможней. Проверяет регистрацию партий на границе, блокирует незарегистрированный импорт и сверяет ввозимый объем с отгрузками.",
            detailsKk = "Кеденмен интеграция. Шекарада партиялардың тіркелуін тексереді, тіркелмеген импортты блоктайды және әкелінетін көлемді жөнелтілімдермен салыстырады.",
            checklistRu = listOf(
                "Подключен шлюз данных с таможенным контролем на границе",
                "Настроен триггер блокировки и парковки нелегальных партий",
                "Реализован баланс объемов ввозимого сырья и готовых доз"
            ),
            checklistKk = listOf(
                "Шекарада кедендік бақылаумен деректер шлюзі қосылды",
                "Заңсыз партияларды блоктау және тұраққа қою триггері реттелді",
                "Әкелінетін шикізат пен дайын дозалар көлемінің теңгерімі жүзеге асырылды"
            )
        ),
        RegulationItem(
            title = "gis_control_purchase",
            descriptionEn = "Market Quality Audits (Control Purchases)",
            descriptionRu = "Контрольные закупки",
            descriptionKk = "Бақылау сатып алулары",
            category = "Systems",
            detailsEn = "Automates random sampling checks on veterinary markets. Coordinates sampling by inspectors, testing at NRCV, and automatic block on failures.",
            detailsRu = "Контроль качества препаратов в обороте. Автоматизирует отбор проб инспекторами, проведение испытаний в НРЦВ и блокировку брака в системе.",
            detailsKk = "Айналымдағы препараттардың сапасын бақылау. Инспекторлардың үлгілерді алуын, НРЦВ-да сынақтар жүргізуді және жүйедегі ақауларды блоктауды автоматтандырады.",
            checklistRu = listOf(
                "Создан модуль назначения и логирования контрольных закупок",
                "Связаны результаты испытаний НРЦВ с реестром разрешений",
                "Реализована рассылка уведомлений о блокировке бракованной серии"
            ),
            checklistKk = listOf(
                "Бақылау сатып алуларын тағайындау и тіркеу модулі құрылды",
                "НРЦВ сынақтарының нәтижелері рұқсаттар тізілімімен байланыстырылды",
                "Ақаулы серияны блоктау туралы хабарламаларды тарату жүзеге асырылды"
            )
        )
    )

    var lastSyncSource = "Local Cache (Offline)"

    suspend fun getRegulations(): List<RegulationItem> {
        return try {
            val response = client.get("http://127.0.0.1:8081/api/regulations")
            if (response.status.value in 200..299) {
                val remoteData: List<RegulationItem> = response.body()
                settings.putString(cacheKey, json.encodeToString(remoteData))
                lastSyncSource = "Cloud API (Online)"
                remoteData
            } else {
                throw Exception("Server returned HTTP ${response.status}")
            }
        } catch (e: Throwable) {
            val cachedJson = settings.getStringOrNull(cacheKey)
            if (cachedJson != null) {
                try {
                    val data = json.decodeFromString<List<RegulationItem>>(cachedJson)
                    lastSyncSource = "Local Cache (Offline)"
                    data
                } catch (ex: Throwable) {
                    lastSyncSource = "Default Seed (Offline)"
                    seedData
                }
            } else {
                lastSyncSource = "Default Seed (Offline)"
                seedData
            }
        }
    }
}
