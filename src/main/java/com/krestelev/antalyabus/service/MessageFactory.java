package com.krestelev.antalyabus.service;

import static com.krestelev.antalyabus.data.Language.ENG;
import static com.krestelev.antalyabus.data.Language.RUS;
import static com.krestelev.antalyabus.data.Language.TUR;

import com.krestelev.antalyabus.data.Language;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageFactory {

    private final UserService userService;

    public static final String START_MESSAGE = """
            Hi, %s\\!
            This bot will help you track buses in Antalya\\.
            Usage is simple: just enter bus stop number, and you'll see upcoming buses\\.
            Use /help get full description of this bot\\.
            
            Select language \\| Выберите язык \\| Dil seçin:
            """;

    public String getStartMessage() {
        return START_MESSAGE;
    }

    public static final Map<Language, String> SHOULD_ADD_STOPS_MESSAGE = Map.of(
        ENG, "There is no stops yet, you should add some",
        RUS, "Сначала добавьте остановку в избранные",
        TUR, "Henüz durak yok, biraz eklemelisin"
    );

    public Language resolveUserLanguage(long chatId) {
        return userService.getUserLanguage(chatId);
    }

    public String getShouldAddStopsMessage(long chatId) {
        return SHOULD_ADD_STOPS_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> SELECT_STOP_MESSAGE = Map.of(
        ENG, "Select bus stop",
        RUS, "Выберите остановку",
        TUR, "Otobüs durağı seçin"
    );

    public String getSelectStopMessage(long chatId) {
        return SELECT_STOP_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> SELECT_BUS_MESSAGE = Map.of(
        ENG, "Select bus",
        RUS, "Выберите автобус",
        TUR, "Otobüs seç"
    );

    public String getSelectBusMessage(long chatId) {
        return SELECT_BUS_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> SELECT_OPTION_MESSAGE = Map.of(
        ENG, "Select an option",
        RUS, "Выберите Опцию",
        TUR, "Seçeneği seçin"
    );

    public String getSelectOptionMessage(long chatId) {
        return SELECT_OPTION_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> INTERVAL_CHANGED_MESSAGE = Map.of(
        ENG, "The tracking interval has been changed",
        RUS, "Интервал отслеживания изменен",
        TUR, "İzleme aralığı değiştirildi"
    );

    public String getReplyMessageForChangingInterval(long chatId) {
        return INTERVAL_CHANGED_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> TRACKING_STARTED_MESSAGE = Map.of(
        ENG, "Tracking has been started with interval %d min",
        RUS, "Отслеживание начато, интервал уведомлений %d мин",
        TUR, "İzleme %d dk aralık ile başlatıldı"
    );

    public String getTrackingStartedMessage(long chatId) {
        return TRACKING_STARTED_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> WANT_TRACKING_MESSAGE = Map.of(
        ENG, "Start tracking?",
        RUS, "Начать отслеживание?",
        TUR, "İzlemeye başlamak mı?"
    );

    public String getWantTrackingMessage(long chatId) {
        return WANT_TRACKING_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> DEFAULT_INTERVAL_MESSAGE = Map.of(
        ENG,  "Choose desired tracking interval",
        RUS, "Выберите желаемый интервал отслеживания",
        TUR, "İstediğiniz izleme aralığını seçin"
    );

    public String getSetDefaultIntervalMessage(long chatId) {
        return DEFAULT_INTERVAL_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> TRACKING_CANCELLED_MESSAGE = Map.of(
        ENG,  "Tracking has been cancelled",
        RUS, "Отслеживание отменено",
        TUR, "Takip iptal edildi"
    );

    public String getTrackingCancelledMessage(long chatId) {
        return TRACKING_CANCELLED_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> STOP_DELETED_MESSAGE = Map.of(
        ENG,  "Stop %s has been deleted",
        RUS, "Остановка %s удалена",
        TUR, "Dur %s silindi"
    );

    public String getStopIsDeletedMessage(long chatId) {
        return STOP_DELETED_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> STOP_NOT_FOUND_MESSAGE = Map.of(
        ENG,  "There is no stop %s in the system",
        RUS, "Остановка %s не найдена в системе",
        TUR, "Sistemde %s durdurma yok"
    );

    public String getStopNotFoundMessage(long chatId) {
        return STOP_NOT_FOUND_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> STOP_LIST_FULL_MESSAGE = Map.of(
        ENG,  "Stops list is full, first delete any stop",
        RUS, "Список остановок полон, сначала удалите любую остановку из списка",
        TUR, "Duraklar listesi dolu, önce herhangi bir durağı silin"
    );

    public String getStopListIsFullMessage(long chatId) {
        return STOP_LIST_FULL_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> STOP_ADDED_MESSAGE = Map.of(
        ENG,  "Stop %s is added",
        RUS, "Остановка %s добавлена",
        TUR, "%s durdurma eklendi"
    );

    public String getStopAddedMessage(long chatId) {
        return STOP_ADDED_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_BUS_MESSAGE = Map.of(
        ENG,  "*%s* \\- %s min",
        RUS, "*%s* \\- %s мин",
        TUR, "*%s* \\- %s dk"
    );

    public String getGetBusMessage(long chatId) {
        return GET_BUS_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_BUS_STOP_DIFF_MESSAGE = Map.of(
        ENG,  "*%s* \\- %s stops",
        RUS, "*%s* \\- %s ост",
        TUR, "*%s* \\- %s duraklar"
    );

    public String getGetBusStopDiffMessage(long chatId) {
        return GET_BUS_STOP_DIFF_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_BUS_ARRIVING_MESSAGE = Map.of(
        ENG, "Bus %s is about to arrive\\!",
        RUS, "Автобус %s вот-вот прибудет\\!",
        TUR, "%s otobüs gelmek üzere\\!"
    );

    public String getBusArrivingMessage(long chatId) {
        return GET_BUS_ARRIVING_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> TRACKING_TIME_EXCEEDED_MESSAGE = Map.of(
        ENG, "Tracking time exceeded the limit of 30 min, tracking is cancelled",
        RUS, "Время отслеживания превысило лимит в 30 мин, слежение окончено",
        TUR, "Takip süresi 30 dk sınırını aştı, takip iptal edildi"
    );

    public String getTrackingTimeExceededMessage(long chatId) {
        return TRACKING_TIME_EXCEEDED_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_SHARE_LOCATION_MESSAGE = Map.of(
        ENG, "Share your location to find nearest stops",
        RUS, "Отправьте геолокацию для поиска ближайших остановок",
        TUR, "En yakın durakları bulmak için konumunuzu paylaşın"
    );

    public String getShareLocationMessage(long chatId) {
        return GET_SHARE_LOCATION_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_SHARE_LOCATION_BUTTON_MESSAGE = Map.of(
        ENG, "Share location",
        RUS, "Отправить геолокацию",
        TUR, "Konumu paylaş"
    );

    public String getShareLocationButtonMessage(Language language) {
        return GET_SHARE_LOCATION_BUTTON_MESSAGE.get(language);
    }

    public static final Map<Language, String> GET_TRACK_BUTTON_MESSAGE = Map.of(
        ENG, "Track all buses",
        RUS, "Отслеживать все автобусы",
        TUR, "Tüm otobüsleri takip edin"
    );

    public String getTrackButtonMessage(long chatId) {
        return GET_TRACK_BUTTON_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_MORE_BUSES_BUTTON_MESSAGE = Map.of(
        ENG, "Track a particular bus",
        RUS, "Отслеживать определенный автобус",
        TUR, "Belirli bir otobüsü takip edin"
    );

    public String getMoreBusesButtonMessage(long chatId) {
        return GET_MORE_BUSES_BUTTON_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_MORE_OPTIONS_BUTTON_MESSAGE = Map.of(
        ENG, "Show other options",
        RUS, "Показать другие опции",
        TUR, "Diğer seçenekleri göster"
    );

    public String getTrackingOptionsButtonMessage(long chatId) {
        return GET_MORE_OPTIONS_BUTTON_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> REFRESH_BUTTON_MESSAGE = Map.of(
        ENG, "Refresh info",
        RUS, "Обновить информацию",
        TUR, "Bilgileri yenile"
    );

    public String getRefreshButtonMessage(long chatId) {
        return REFRESH_BUTTON_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_MIN_BUTTON_MESSAGE = Map.of(
        ENG, "min",
        RUS, "мин",
        TUR, "dk"
    );

    public String getMinutesOnButtonMessage(long chatId) {
        return GET_MIN_BUTTON_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> LANGUAGE_CHANGED_MESSAGE = Map.of(
        ENG, "The language has been changed",
        RUS, "Язык изменен",
        TUR, "Dil değiştirildi"
    );

    public String getReplyMessageForChangingLanguage(long chatId) {
        return LANGUAGE_CHANGED_MESSAGE.get(resolveUserLanguage(chatId));
    }

    public static final Map<Language, String> GET_METERS_MESSAGE = Map.of(
        ENG, "meters",
        RUS, "метров",
        TUR, "metre"
    );


    public String getMetersButtonMessage(Language language) {
        return GET_METERS_MESSAGE.get(language);
    }

    public static final Map<Language, String> HELP_MESSAGE = Map.of(
        ENG, """
        To see upcoming buses for a particular stop, enter stop number\\.
        
        You can add stops to your favorites using "add" command \\(e\\.g\\. "add 10009"\\)\\. After you've added some stop, you get it easily via /stops command from the menu\\. If you want to remove the stop, similarly use "delete" command \\(e\\.g\\. "delete 10009"\\)\\. You can add only up to 6 buses to your favorites list\\.
        
        You can get stops nearest to your current location via /nearest command from the menu\\.
        
        You can change bus tracking interval via /interval command from the menu \\(default interval \\- 2 min\\)\\.
        
        To stop tracking and cancel all notifications related to upcoming buses, use /cancel command from the menu\\.
        
        Usually buses start to be visible \\~30 min prior to coming\\. However, it is possible to track farther buses, if the option of tracking is selected for a particular bus\\. In this case, distance between stop and bus will be measured in number of stops between them\\.
        
        Note that: strikethrough numbers in the output mean that bus didn't dropped off the bus station yet\\.""",
        RUS, """
        Чтобы узнать о ближайших автобусах, введите номер остановки\\.
        
        Вы можете добавить остановку в избранные с помощью команды "add" \\(например, "add 10009"\\)\\. Для доступа к избранным остановкам выберите команду /stops из меню\\. Для удаления остановки используйте команду "delete"\\. Вы можете добавить до 6 остановок\\.
        
        Чтобы посмотреть ближайшие к вам остановки, используйте команду /nearest из меню\\.
        
        Вы можете настроить интервал уведомлений ближайших автобусах с помощью команды /interval из меню \\(дефолтный интервал \\- 2 минуты\\)\\.
        
        Чтобы остановить уведомления о ближайших автобусах, используйте команду /cancel из меню\\.
        
        Обычно автобусы становятся видимыми за ~30 минут, но возможно увидеть автобус заранее\\. Для этого необходимо выбрать отслеживание определенного автобуса для остановки\\. Пока автобус не стал видимым в системе, расстояние между остановкой и автобусом будет показано в количестве остановок между ними\\. 
        
        Условные обозначения: зачеркнутый номер напротив автобуса означает, что автобус пока не выехал из автостанции\\.""",
        TUR, """
        Belirli bir durak için yaklaşan otobüsleri görmek için durak numarasını girin\\.
        
        "add" komutunu kullanarak favorilerinize durak ekleyebilirsiniz \\(örneğin, "add 10009"\\)\\. Menüden sunulan /stops komutu ile favori durakların listesini alırsınız\\. Durdurmayı kaldırmak istiyorsanız, benzer şekilde "delete" komutunu kullanın\\. Sık kullanılanlar listenize yalnızca en fazla 6 otobüs ekleyebilirsiniz\\.
        
        Menüden /nearest yakın komutu kullanarak en yakın otobüs duraklarına ulaşabilirsiniz\\.
        
        Menüden /interval komutu ile veri yolu izleme aralığını değiştirebilirsiniz \\(varsayılan aralık \\- 2 dakika\\)\\.
        
        İzlemeyi durdurmak ve tüm bildirimleri iptal etmek için menüden /cancel komutunu kullanın\\.
        
        Genellikle otobüsler gelmeden ~30 dakika önce görünür olmaya başlar\\. Bununla birlikte, belirli bir otobüs için izleme seçeneği seçilirse, daha uzak otobüsleri izlemek mümkündür\\. Bu durumda, durak ile otobüs arasındaki mesafe, aralarındaki durak sayısı olarak ölçülecektir\\.
        
        Şunu unutmayın: belirli bir numara için üstü çizili numara görüntülenir, ancak bunun anlamı otobüs durağından henüz düşmemiş olmasıdır\\."""
    );

    public String getHelpMessage(long chatId) {
        return HELP_MESSAGE.get(resolveUserLanguage(chatId));
    }

    // error messages

    public static final Map<Language, String> NO_BUSES_ERROR_MESSAGE = Map.of(
        ENG, "Currently there are no buses for bus stop %s",
        RUS, "Пока нет автобусов для остановки %s",
        TUR, "Şu anda %s numaralı otobüs durağı için otobüs yok"
    );

    public String getNoBusesErrorMessage(long chatId) {
        return NO_BUSES_ERROR_MESSAGE.get(resolveUserLanguage(chatId));
    }
}