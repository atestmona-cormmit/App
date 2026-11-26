const fs = require('fs');
const path = require('path');
const AdmZip = require('adm-zip');
const { spawn } = require('child_process');

// استلام المسارات من تطبيق الأندرويد
const zipPath = process.env.ZIP_PATH || '/storage/emulated/0/Download/bot.zip';
const tokensPath = process.env.TOKENS_PATH || '/storage/emulated/0/Download/wolf_tokens.json';
const botFolder = path.join(__dirname, 'active_bot');

function startBot() {
    try {
        console.log(`[Runner] Extracting ${zipPath}...`);
        
        // إنشاء مجلد البوت
        if (!fs.existsSync(botFolder)) {
            fs.mkdirSync(botFolder, { recursive: true });
        }

        // فك الضغط
        const zip = new AdmZip(zipPath);
        zip.extractAllTo(botFolder, true);
        console.log(`[Runner] Extracted to ${botFolder}`);

        // تشغيل البوت
        console.log('[Runner] Starting bot process...');
        const bot = spawn(process.execPath, ['check-wolf.js'], { 
            cwd: botFolder,
            env: { ...process.env, TOKENS_FILE: tokensPath }
        });
        
        bot.stdout.on('data', data => console.log(`[Bot]: ${data.toString()}`));
        bot.stderr.on('data', data => console.error(`[Bot Error]: ${data.toString()}`));
        
    } catch (e) {
        console.error("[Runner] Error:", e.message);
    }
}

// تأخير بسيط لضمان جاهزية النظام ثم البدء
setTimeout(startBot, 1000);
