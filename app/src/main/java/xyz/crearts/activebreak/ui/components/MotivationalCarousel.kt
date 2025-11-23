package xyz.crearts.activebreak.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MotivationalCard(
    val title: String,
    val message: String,
    val emoji: String,
    val gradient: List<Color>
)

@Composable
fun MotivationalCarousel(modifier: Modifier = Modifier) {
    val cards = remember {
        listOf(
            // Движение и активность
            MotivationalCard(
                "Двигайся!",
                "Каждое движение приближает тебя к здоровью",
                "🏃",
                listOf(Color(0xFF667eea), Color(0xFF764ba2))
            ),
            MotivationalCard(
                "Растяжка",
                "5 минут растяжки = заряд энергии",
                "🧘",
                listOf(Color(0xFFf093fb), Color(0xFFf5576c))
            ),
            MotivationalCard(
                "Встань и потянись!",
                "Разомни затёкшие мышцы",
                "🤸",
                listOf(Color(0xFFffa751), Color(0xFFffe259))
            ),

            // Вода и здоровье
            MotivationalCard(
                "Вода - жизнь",
                "Не забывай пить воду регулярно",
                "💧",
                listOf(Color(0xFF36d1dc), Color(0xFF5b86e5))
            ),
            MotivationalCard(
                "Увлажнение важно!",
                "Стакан воды прямо сейчас?",
                "🥤",
                listOf(Color(0xFF89f7fe), Color(0xFF66a6ff))
            ),

            // Глаза и зрение
            MotivationalCard(
                "Глазам отдых!",
                "Отводи взгляд от экрана каждый час",
                "👀",
                listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
            ),
            MotivationalCard(
                "Береги зрение!",
                "Правило 20-20-20: работает!",
                "👁️",
                listOf(Color(0xFF43e97b), Color(0xFF38f9d7))
            ),

            // Дыхание и релакс
            MotivationalCard(
                "Дыши глубже",
                "5 глубоких вдохов снимут стресс",
                "🌬️",
                listOf(Color(0xFFa8edea), Color(0xFFfed6e3))
            ),
            MotivationalCard(
                "Расслабься",
                "Минута тишины перезагрузит мозг",
                "😌",
                listOf(Color(0xFFfbc2eb), Color(0xFFa6c1ee))
            ),
            MotivationalCard(
                "Медитация",
                "Закрой глаза на 2 минуты",
                "🧘‍♂️",
                listOf(Color(0xFFffecd2), Color(0xFFfcb69f))
            ),

            // Мотивация и позитив
            MotivationalCard(
                "Ты молодец!",
                "Продолжай заботиться о своём здоровье",
                "⭐",
                listOf(Color(0xFFfa709a), Color(0xFFfee140))
            ),
            MotivationalCard(
                "Продуктивность",
                "Перерывы делают тебя эффективнее!",
                "🚀",
                listOf(Color(0xFFf77062), Color(0xFFfe5196))
            ),
            MotivationalCard(
                "Ты можешь!",
                "Каждый перерыв - это инвестиция в себя",
                "💪",
                listOf(Color(0xFFff9a9e), Color(0xFFfecfef))
            ),
            MotivationalCard(
                "Так держать!",
                "Забота о здоровье - твой приоритет",
                "🎯",
                listOf(Color(0xFFffa8b7), Color(0xFFffc6d9))
            ),

            // Энергия и бодрость
            MotivationalCard(
                "Зарядись!",
                "Короткий перерыв = больше энергии",
                "⚡",
                listOf(Color(0xFFfddb92), Color(0xFFd1fdff))
            ),
            MotivationalCard(
                "Взбодрись!",
                "Прогулка на свежем воздухе творит чудеса",
                "🌤️",
                listOf(Color(0xFFffeaa7), Color(0xFFfdcb6e))
            ),

            // Осанка и спина
            MotivationalCard(
                "Проверь осанку!",
                "Ровная спина = здоровье на годы",
                "🦴",
                listOf(Color(0xFFe0c3fc), Color(0xFF8ec5fc))
            ),
            MotivationalCard(
                "Разомни спину",
                "Твоя спина скажет спасибо",
                "💆",
                listOf(Color(0xFFfad0c4), Color(0xFFffd1ff))
            ),

            // Перекус и еда
            MotivationalCard(
                "Перекус",
                "Фрукт или орехи для мозга",
                "🍎",
                listOf(Color(0xFFff9a9e), Color(0xFFfad0c4))
            ),

            // Общее здоровье
            MotivationalCard(
                "Твоё здоровье",
                "Это твой самый ценный актив!",
                "❤️",
                listOf(Color(0xFFff6e7f), Color(0xFFbfe9ff))
            ),
            MotivationalCard(
                "Позаботься о себе",
                "Ты этого достоин!",
                "💝",
                listOf(Color(0xFFffecd2), Color(0xFFfcb69f))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { cards.size })
    val scope = rememberCoroutineScope()

    // Автопрокрутка
    LaunchedEffect(pagerState.currentPage) {
        delay(5000)
        scope.launch {
            val nextPage = (pagerState.currentPage + 1) % cards.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 600)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) { page ->
            MotivationalCardItem(card = cards[page])
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Индикаторы страниц
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(cards.size) { index ->
                val color = if (pagerState.currentPage == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun MotivationalCardItem(card: MotivationalCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(card.gradient)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = card.emoji,
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = card.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
