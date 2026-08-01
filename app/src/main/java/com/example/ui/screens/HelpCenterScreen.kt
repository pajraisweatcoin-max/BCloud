package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

data class HelpTopic(val title: String, val description: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen() {
    val topics = listOf(
        HelpTopic("1. Panduan Instalasi di Android STB", "Pasang APK BARRA CLOUD di Android STB RAM 1 GB. Berikan izin Akses Penyimpanan (Storage Access) agar aplikasi dapat membaca dan menulis ke harddisk USB."),
        HelpTopic("2. Menghubungkan & Menggunakan Banyak HDD USB", "Colokkan 1 atau lebih Flashdisk/Harddisk Eksternal ke port USB STB. BARRA CLOUD secara otomatis mendeteksi Hot-Plug dan menampilkan seluruh storage di Dashboard. Aktifkan opsi 'Auto Merge Library' jika ingin menggabungkan isi file dari seluruh HDD ke dalam satu Galeri."),
        HelpTopic("3. Server Mode vs Viewer Mode", "Satu APK dapat berfungsi sebagai SERVER (di STB) yang melayani media 24 jam non-stop, atau sebagai VIEWER (di HP/Tablet) yang mengakses media secara jarak jauh via IP Tailscale."),
        HelpTopic("4. Pengaturan Tailscale & Jarak Jauh", "Instal Tailscale di STB dan HP Anda. Gunakan IP MagicDNS (misal 100.x.y.z) untuk menghubungkan Viewer ke Server BARRA CLOUD secara aman dari jaringan internet mana pun tanpa port-forwarding router."),
        HelpTopic("5. Fitur Offline Sync Queue & Auto Resume", "Apabila koneksi Tailscale terputus saat mengunggah, mengunduh, atau backup media, proses akan dimasukkan ke Antrean Offline. Saat jaringan pulih, transfer akan dilanjutkan secara otomatis tanpa terulang dari nol."),
        HelpTopic("6. Auto Reboot & Pemeliharaan Mandiri", "Gunakan jadwal Auto Reboot harian (misal jam 03:00 malam) untuk menjaga performa STB tetap segar dan membebaskan RAM secara otomatis."),
        HelpTopic("7. Troubleshooting & Solusi", "• Harddisk tidak terbaca: Pastikan format NTFS/FAT32/exFAT dan kabel USB tercolok rapat.\n• Server tidak merespons: Periksa apakah Foreground Service aktif dan IP Tailscale sudah benar.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pusat Bantuan & Panduan Offline", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface)
            )
        },
        containerColor = SlateBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(topics) { topic ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(topic.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(topic.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
