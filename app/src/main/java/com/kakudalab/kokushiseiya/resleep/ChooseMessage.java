package com.kakudalab.kokushiseiya.resleep;

import java.util.Random;

/**
 * 通知メッセージを決定するクラス
 */
public class ChooseMessage {
    Random rnd;

    public ChooseMessage(){
        rnd = new Random();
    }

    public String getMessage(int num){
        String message = "";

        switch (num){
            case 0:
                message = morningMessage();
                break;

            case 1:
                message = dayTimeMessage();
                break;

            case 2:
                message = nightMessage();
                break;

            case 3:
                message = midNightMessage();
                break;

            case 4:
                message = noTimeMessage();
                break;

            case 5:
                message = exerciseMessage();
                break;

            case 6:
                message = eatMessage();
                break;

            case 7:
                message = cafeMessage();
                break;

            case 8:
                message = alcoholMessage();
                break;

            case 9:
                message = tabaccoMessage();
                break;

            case 10:
                message = napMessage();
                break;

            case 11:
                message = longSleepMessage();
                break;

            case 12:
                message = shortSleepMessage();
                break;

            case 13:
                message = beforeSleepMessage();
        }

        return message;
    }

    public String beforeSleepMessage() {
        return "あと1時間で就寝予定時刻です。\n本を読むなどして、リラックスして就寝に備えましょう。";
    }

    public String shortSleepMessage() {
        return "睡眠時間が短かったようです。\n今日は少し早めに寝るようにしましょう。";
    }

    public String longSleepMessage() {
        return "睡眠時間が長かったようです。\n今日は少し遅めに寝るようにしましょう。";
    }

    public String alcoholMessage(){
        return "今、アルコールを飲みましたか？";
    }

    public String cafeMessage() {
        return "今、カフェインを摂取しましたか？";
    }

    public String eatMessage() {
        return "今、食事をしましたか？";
    }

    public String exerciseMessage() {
        return "今、運動もしくは、歩いたり自転車に乗ったりしましたか？";
    }

    public String napMessage() {
        return "今、寝ていましたか？";
    }

    public String tabaccoMessage() {
        return "今、タバコを吸いましたか？";
    }


    public String morningMessage(){
        int r = rnd.nextInt(2);

        if (r == 0){
            return "朝食をとることで、体が目覚め元気に1日を始めることができます。朝食をきちんととることが大事です。";
        }else{
            return "起床後すぐに太陽の光を浴びることで、体内時計のリズムがリセットされ、15~16時間後に自然な眠気が出現します。起床後はできる限り早く、太陽の光を浴びるようにしましょう。";
        }
    }

    public String dayTimeMessage(){
        int r = rnd.nextInt(2);

        if (r == 0){
            return "定期的な適度の運動は、良い睡眠をもたらします。運動習慣を身につけましょう。しかし、激しい運動はかえって睡眠を妨げる可能性があるので、注意しましょう。";
        }else{
            return "夜間に必要な睡眠時間を確保できなかった場合には、昼間の仮眠がその後の作業効率の改善を図ることに役立つ可能性があります。ただし、仮眠時間は30分以内に抑えましょう。";
        }
    }

    public String nightMessage(){
        int r = rnd.nextInt(11);
        String message = "";

        switch (r){
            case 0:
                message = "飲酒によって短期的に眠気が強くなりますが、長期的には飲酒は睡眠の質・量ともに悪化させます。寝酒は控えましょう。";
                break;

            case 1:
                message = "喫煙によって摂取されたニコチンは1時間ほど作用します。就寝1時間前や途中で目が覚めた時の喫煙は避けましょう。";
                break;

            case 2:
                message = "夕方から就寝前のカフェイン摂取は、入眠を妨げたり、睡眠時間を短くさせたりする傾向があります。夕食以降にはカフェイン摂取に気をつけましょう。";
                break;

            case 3:
                message = "スムーズに入眠するためには、心身ともにリラックスすることが大切です。就寝1時間前は何もしないで良い時間を確保しましょう。";
                break;

            case 4:
                message = "リラックスする方法や状況は人によって異なります。個人にあったリラックス方法を見つけることが重要です。";
                break;

            case 5:
                message = "就寝0.5~6時間前の入浴による体温変化は、入眠の促進や深い睡眠の増加といった睡眠の改善効果をもたらします。40℃程度の高過ぎない湯温で入浴するのがオススメです。";
                break;

            case 6:
                message = "寝室の温度、湿度、騒音、光、寝具、寝衣などの環境は睡眠の質と関係することが示されています。寝室・寝床内は、静かで、暗く、温度や湿度が季節に応じた適切なものに保たれるように気をつけましょう。";
                break;

            case 7:
                message = "入眠前に普通の室内よりも明るい光の下で過ごすと、入眠が妨げられます。寝る前には、強い明かりに当たるのを避けたほうが良いでしょう。";
                break;

            case 8:
                message = "光の覚醒作用を利用して、朝の起床前に寝室を少しずつ明るくすると、それに応じて睡眠が浅くなり、起床時の目覚め感が良くなります。朝日が部屋に入りやすいよう、カーテンを開けたり、レースだけ閉めたりなどして寝ましょう。";
                break;

            case 9:
                message = "休日に夜更かしをしてしまうと、休日後の平日の覚醒・起床が困難になり、体内時計も遅れてしまいます。休日もできる限りいつも通りの生活リズムを保ちましょう。";
                break;

            case 10:
                message = "長い時間寝床で過ごすと、熟睡感が損なわれ、不眠につながるので、必要以上に寝床で過ごさないように気をつけましょう。";
                break;
        }

        return message;
    }

    public String midNightMessage(){
        int r = rnd.nextInt(4);
        String message = "睡眠予定時刻になりました。\n";

        switch (r){
            case 0:
                message += "徹夜は認知・精神運動作業能力の低下を及ぼします。無理せず、休養をとりましょう。";
                break;

            case 1:
                message += "睡眠時間や就寝時刻にこだわると、逆に寝つきを悪くすることがあります。30分以上寝床で目が覚めていたら、一度起きるなどして気分を変えましょう。";
                break;

            case 2:
                message += "目覚めなければいけない時間から逆算して寝床につく就寝時間を決めるのではなく、起床時間のみ定め、眠気が出始めるまで寝床につかないようにすることで、睡眠健康満足度の向上に有効であるとされています。";
                break;

            case 3:
                message += "眠ろうとする意気込みや、眠れないのではないかという不安は、脳の覚醒を促し、自然な入眠を遠ざけます。適切な環境を整え、リラックスすれば自然に眠れるでしょう。";
                break;
        }

        return message;
    }

    public String noTimeMessage(){
        int r = rnd.nextInt(12);
        String message = "";

        switch (r){
            case 0:
                message = "いびきや睡眠時無呼吸が気になる場合は、医療機関に相談に行きましょう。";
                break;

            case 1:
                message = "喫煙本数が多いほど不眠の割合が高くなります、タバコはほどほどに。";
                break;

            case 2:
                message = "カフェインはコーヒーだけでなく、緑茶、ココア、栄養ドリンク剤など多くの食品に含まれます。過剰な摂取は睡眠に悪影響を及ぼすので、気をつけましょう。";
                break;

            case 3:
                message = "睡眠不足や不眠は生活習慣病の危険を高めます。生活習慣を整え、きちんと睡眠をとりましょう。";
                break;

            case 4:
                message = "肥満は睡眠時無呼吸の発症・悪化に影響を及ぼします。定期的な運動を心がけ、暴飲暴食はしないようにしましょう。";
                break;

            case 5:
                message = "成人男性の平均的な睡眠時間は6〜8時間と言われていますが、個人差があるものなので、自分に合った適切な睡眠時間を見つけることが大切です。";
                break;

            case 6:
                message = "睡眠時間は短すぎても長すぎてもよくありません。日中活発に過ごせるかどうかを睡眠充足の目安として、時には睡眠習慣を振り返ることも重要です。";
                break;

            case 7:
                message = "自分の睡眠時間が足りているかを知るための手段としては、日中の眠気の強さを確認する方法があります。日中に強い眠気を感じる場合には、睡眠時間を見直しましょう。";
                break;

            case 8:
                message = "睡眠不足が続くと、日中の作業能率が十分に回復するまで時間がかかります。寝だめで寝不足を補うのではなく、毎日充分な睡眠時間の確保に努めましょう。";
                break;

            case 9:
                message = "眠りが浅く、夜間に何度も目が覚めるようであれば、寝床で過ごす時間を減らし遅寝・早起きを心がけると良いでしょう。";
                break;

            case 10:
                message = "夜間に十分な睡眠時間が確保されていても、日中の眠気や居眠りで困っている場合には、医師を受診し適切な検査を受け、対策を立てることが大切です。";
                break;

            case 11:
                message = "睡眠に関する問題によって、日中の生活に好ましくない影響があると感じた場合には、出来る限り早めに医師、歯科医師、保健師、薬剤師など身近な専門家に相談することが大切です。";
                break;
        }

        return message;
    }
}
