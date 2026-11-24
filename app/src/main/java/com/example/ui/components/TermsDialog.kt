package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyInk
import com.example.ui.theme.AnqeznyInkSecondary

@Composable
fun TermsDialog(
    onDismiss: () -> Unit,
    onAccept: (() -> Unit)? = null
) {
    BackHandler { onDismiss() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("الشروط", "الدفع والعمولة", "الضمانات", "الخصوصية")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "الشروط والأحكام — منصة انقذنى عايز ونش",
                    style = MaterialTheme.typography.titleLarge,
                    color = AnqeznyCharcoal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tabs.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            TermSection("1. طبيعة المنصة ونطاق الخدمة", "تعمل منصة «انقذنى عايز ونش» كوسيط تقني يربط بين العملاء وأصحاب سيارات الإنقاذ (الأوناش) المستقلين وشركات التشغيل المرخصة. الخدمة متاحة حالياً داخل نطاق محافظة الجيزة (14 منطقة رئيسية والتحديد الحر على الخريطة).")
                            TermSection("2. التزامات السائق والشركة", "يلتزم السائق بتقديم مستندات سارية (بطاقة رقم قومي، رخصة قيادة مهنية، رخصة تسيير الونش) والمحافظة على الرصيد الأدنى بمحفظة الضمان لاستقبال الطلبات.")
                            TermSection("3. سياسة الإلغاء والانتظار", "في حالة إلغاء العميل للطلب بعد تحرك السائق لموقع الاستلام، تُطبق رسوم إلغاء قدرها 40 ج.م (أو حسب التسعير المعلن). يُحتسب وقت الانتظار تلقائياً فور تسجيل الوصول.")
                        }
                        1 -> {
                            TermSection("1. حساب السعر المقفول", "يتم احتساب السعر المقفول قبل تأكيد الطلب بناءً على المسافة الفعلية وسعر السولار وفئة السيارة ونوع الخدمة وضريبة القيمة المضافة.")
                            TermSection("2. الدفع النقدي والعمولة", "في الرحلات المدفوعة نقداً (كاش)، يحصل السائق على كامل المبلغ من العميل وتُخصم عمولة المنصة تلقائياً من محفظة الضمان.")
                            TermSection("3. الدفع الإلكتروني والسحب", "تُضاف أرباح الرحلات المدفوعة إلكترونياً (فور تفعيل البوابة) إلى رصيد الأرباح القابل للسحب بدون رسوم تحويل بحد أدنى 100 ج.م عبر فودافون كاش أو إنستا باي أو التحويل البنكي.")
                        }
                        2 -> {
                            TermSection("1. سلسلة الحيازة والتوثيق", "تُعد الصور الأربع الملتقطة قبل التحميل وكود الأمان (PIN) وتوقيع العميل الإلكتروني عند التسليم أدلة رسمية معتمدة لحفظ حقوق الطرفين عند أي نزاع.")
                            TermSection("2. حدود المسؤولية والتعويض", "تتحمل الجهة المنفذة للنقل مسؤولية سلامة المركبة أثناء التحميل والتنزيل في حدود التأمين والضوابط القانونية المعمول بها، وتلتزم المنصة بحفظ سجلات التوثيق والصور وتقديمها للجهات المختصة.")
                            TermSection("3. الاختصاص القضائي", "تخضع هذه الشروط لقوانين جمهورية مصر العربية، وتختص محاكم محافظة الجيزة بالفصل في أي نزاع.")
                        }
                        else -> {
                            TermSection("1. حماية البيانات الشخصية", "يتم عزل بيانات الهوية والمستندات القانونية للسائقين والشركات في نطاق محمي لا يطلع عليه العملاء، ويُستخدم رقم الهاتف للتواصل أثناء تنفيذ الرحلة فقط.")
                            TermSection("2. التقييمات والشفافية", "تُعرض تقييمات النجوم والتعليقات بعد إتمام الرحلة بشفافية في الملف العام للسائق لمساعدة العملاء في اختيار أفضل خدمة.")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onAccept != null) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("accept_terms_button")
                        ) {
                            Text("أوافق على الشروط")
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("close_terms_button")
                        ) {
                            Text("إغلاق")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TermSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AnqeznyInk, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
    }
}
