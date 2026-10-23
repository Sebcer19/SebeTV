package fr.sebcer.sebetv;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Local-only TV launcher. No accounts, network calls or privileged settings changes. */
public class MainActivity extends Activity {
    private android.content.SharedPreferences prefs;
    private final ArrayList<ResolveInfo> apps = new ArrayList<>();
    private LinearLayout content;
    private boolean allApps = false;
    private String movingPackage = null;
    private String restoreFocus = "";
    private int theme;
    private Landscape landscape;
    private BackgroundVideo backgroundVideo;
    private String pendingVideoPackage;
    private boolean resumed;
    private int focusColor;
    private final Map<String,Integer> appColors = new HashMap<>();
    private final int[] accents = {0xffe8b778, 0xffa9b8ff, 0xff84dbc9};
    private int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("sebetv", MODE_PRIVATE);
        if (state != null) { allApps = state.getBoolean("all"); restoreFocus = state.getString("focus", ""); pendingVideoPackage=state.getString("video_package"); }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("video_package",pendingVideoPackage);
        state.putBoolean("all", allApps); state.putString("focus", restoreFocus);
        super.onSaveInstanceState(state);
    }
    @Override protected void onResume() { super.onResume(); resumed=true; reloadApps(); render(); }
    @Override protected void onPause() {
        resumed=false;
        if(backgroundVideo!=null)backgroundVideo.stop();
        if(landscape!=null)landscape.setMotion(false);
        super.onPause();
    }
    private void reloadApps() {
        apps.clear();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        Set<String> seen = new HashSet<>();
        for (ResolveInfo info : getPackageManager().queryIntentActivities(query, 0)) {
            String pkg = info.activityInfo.packageName;
            if (!pkg.equals(getPackageName()) && seen.add(pkg)) apps.add(info);
        }
        apps.sort((a,b) -> a.loadLabel(getPackageManager()).toString().compareToIgnoreCase(b.loadLabel(getPackageManager()).toString()));
    }
    private Set<String> favorites() { return new HashSet<>(prefs.getStringSet("favorites", Collections.emptySet())); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private GradientDrawable surface(boolean focused) {
        GradientDrawable bg = new GradientDrawable(); bg.setColors(new int[]{focused ? 0xf0343e50 : 0xdd202a3b, focused ? 0xf01e283b : 0xcc111a29});
        bg.setOrientation(GradientDrawable.Orientation.TL_BR);
        bg.setCornerRadius(dp(16)); bg.setStroke(dp(focused ? 3 : 1), focused ? focusColor : 0x304f586d); return bg;
    }
    private void focusable(View v, String tag) {
        v.setTag(tag); v.setFocusable(true); v.setClickable(true); v.setBackground(surface(false));
        v.setOnFocusChangeListener((view, focus) -> {
            if (focus) {
                focusColor = appColors.getOrDefault(tag, accents[theme]);
                selectBackground(tag);
            }
            view.setBackground(surface(focus));
            view.animate().scaleX(focus ? 1.045f : 1f).scaleY(focus ? 1.045f : 1f).setDuration(180).start();
            view.setElevation(dp(focus ? 9 : 2));
            if (focus) restoreFocus = tag;
        });
    }
    private TextView button(String label, String tag, Runnable action) {
        TextView b = text(label, 14, Color.WHITE); b.setGravity(Gravity.CENTER); b.setPadding(dp(18), dp(10), dp(18), dp(10));
        focusable(b, tag); b.setOnClickListener(v -> action.run()); return b;
    }
    private TextView tab(String label, String tag, boolean selected, Runnable action) {
        TextView tab = button(label, tag, action);
        tab.setSelected(selected);
        tab.setTextColor(selected ? accents[theme] : Color.WHITE);
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tab.setContentDescription(label + (selected ? ", sélectionné" : ""));
        return tab;
    }
    private boolean isEntertainment(ResolveInfo app) {
        String name = (app.activityInfo.packageName + " " + app.loadLabel(getPackageManager())).toLowerCase(Locale.ROOT);
        String[] brands = {"netflix", "youtube", "disney", "amazonvideo", "prime video", "canal", "crunchyroll", "apple", "spotify", "plex", "jellyfin", "kodi", "molotov", "france.tv", "francetv", "arte", "twitch", "max.hbo", "hbo", "tivimate", "mytf1", "tf1", "m6", "pluto", "paramount", "vlc"};
        for (String brand : brands) if (name.contains(brand)) return true;
        return false;
    }
    private void render() {
        theme = prefs.getInt("theme", 0);
        sortApps();
        focusColor = accents[theme]; appColors.clear();
        if(backgroundVideo!=null)backgroundVideo.stop();
        FrameLayout frame = new FrameLayout(this);
        landscape = new Landscape(this);
        frame.addView(landscape, new FrameLayout.LayoutParams(-1,-1));
        backgroundVideo=new BackgroundVideo(this,()-> {
            if(landscape!=null)landscape.setMotion(resumed && prefs.getBoolean("motion",true));
            Toast.makeText(this,"Vidéo indisponible : animation intégrée utilisée.",Toast.LENGTH_SHORT).show();
        },()-> {if(landscape!=null)landscape.setMotion(false);});
        frame.addView(backgroundVideo,new FrameLayout.LayoutParams(-1,-1));
        View shade=new View(this); GradientDrawable shadeBg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x99050a13,0x33050a13,0x55050a13});shade.setBackground(shadeBg);
        frame.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout shell = new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(38),dp(24),dp(38),dp(14)); frame.addView(shell);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("SÉBÉ TV", 19, accents[theme]); brand.setTypeface(null, Typeface.BOLD);
        header.addView(brand, new LinearLayout.LayoutParams(0,-2,1));
        addNav(header, tab("Accueil", "home", !allApps, () -> { allApps = false; restoreFocus="home"; render(); }));
        addNav(header, tab("Applications", "apps", allApps, () -> { allApps = true; restoreFocus="apps"; render(); }));
        addNav(header, button("Ambiance", "theme", this::ambianceMenu));
        addNav(header, button("Réglages", "settings", this::settingsMenu));
        TextClock clock = new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("HH:mm"); clock.setTextSize(18); clock.setTextColor(Color.WHITE);
        clock.setPadding(dp(12),0,0,0); header.addView(clock); shell.addView(header);
        View rule = new View(this); rule.setBackgroundColor(0x224e5a70);
        LinearLayout.LayoutParams ruleParams = new LinearLayout.LayoutParams(-1,dp(1)); ruleParams.topMargin=dp(17); shell.addView(rule,ruleParams);
        ScrollView scroll = new ScrollView(this); scroll.setClipToPadding(false); scroll.setVerticalScrollBarEnabled(false);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(5),dp(20),dp(5),dp(12));
        scroll.addView(content); shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Set<String> selection = homeSelection();
        if (allApps) {
            addManageButton();
            appGrid(apps, selection);
        } else {
            ArrayList<ResolveInfo> selected = new ArrayList<>();
            for (ResolveInfo app : apps) {
                if (hiddenApps().contains(app.activityInfo.packageName)) continue;
                if (selection.contains(app.activityInfo.packageName)) selected.add(app);

            }
            appShelf(selected, selection);
            if(selected.isEmpty()) content.addView(button("Choisir mes applications TV","manage",this::manageApplications));
        }
        if(movingPackage!=null) content.addView(text("Déplacement : ← → · OK pour terminer",14,accents[theme]));
        if (apps.isEmpty()) content.addView(text("Aucune application TV détectée. Installez-en depuis le Play Store.",16,Color.WHITE));
        setContentView(frame);
        View target = shell.findViewWithTag(restoreFocus);
        if (target == null && !allApps) target = firstCard(content);
        if (target == null) target = shell.findViewWithTag("apps");
        if (target != null) target.requestFocus();
    }
    private View firstCard(ViewGroup parent) {
        for(int i=0;i<parent.getChildCount();i++) {
            View v=parent.getChildAt(i);
            if(v.isFocusable() && v.getTag()!=null) return v;
            if(v instanceof ViewGroup) { View found=firstCard((ViewGroup)v); if(found!=null)return found; }
        }
        return null;
    }
    private Set<String> homeSelection() {
        if (prefs.contains("home_selection"))
            return new HashSet<>(prefs.getStringSet("home_selection", Collections.emptySet()));
        Set<String> initial = favorites();
        if (initial.isEmpty()) for (ResolveInfo app : apps)
            if (isEntertainment(app)) initial.add(app.activityInfo.packageName);
        prefs.edit().putStringSet("home_selection", initial).apply();
        return initial;
    }
    private void addManageButton() {
        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(5),dp(13),dp(5),dp(13));
        TextView manage = button("Gérer mes applications", "manage", this::manageApplications);
        bar.addView(manage);
        TextView shortcuts=button("Raccourcis", "shortcuts", this::shortcuts);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2); sp.leftMargin=dp(12); bar.addView(shortcuts,sp);
        content.addView(bar);
    }
    private void manageApplications() {
        if (apps.isEmpty()) { Toast.makeText(this,"Aucune application TV disponible.",Toast.LENGTH_SHORT).show(); return; }
        // Capture a stable list; cancel/back never commits the draft.
        final ArrayList<ResolveInfo> candidates = new ArrayList<>(apps);
        final Set<String> draft = homeSelection();
        draft.removeAll(hiddenApps());
        String[] labels = new String[candidates.size()]; boolean[] checked = new boolean[candidates.size()];
        for (int i=0;i<candidates.size();i++) {
            ResolveInfo app = candidates.get(i); labels[i]=app.loadLabel(getPackageManager()).toString() + (hiddenApps().contains(app.activityInfo.packageName) ? " · Masquée" : "");
            checked[i]=draft.contains(app.activityInfo.packageName);
        }
        new AlertDialog.Builder(this).setTitle("Applications à mettre en avant")
            .setMultiChoiceItems(labels,checked,(dialog,index,isChecked)-> {
                String pkg=candidates.get(index).activityInfo.packageName;
                if(isChecked) draft.add(pkg); else draft.remove(pkg);
            }).setPositiveButton("Enregistrer",(dialog,which)-> {
                Set<String> hidden=hiddenApps(); hidden.removeAll(draft);
                prefs.edit().putStringSet("home_selection",draft).putStringSet("hidden_apps",hidden).apply(); restoreFocus="manage"; render();
            }).setNegativeButton("Annuler",null).show();
    }
    private void appGrid(List<ResolveInfo> items, Set<String> selection) {
        int columns = columns();
        for(int start=0;start<items.size();start+=columns) {
            LinearLayout row=new LinearLayout(this); row.setPadding(0,dp(7),0,dp(7)); row.setClipChildren(false);
            for(int col=0;col<columns;col++) {
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(tileHeight()),1);lp.setMargins(dp(5),0,dp(5),0);
                row.addView(start+col<items.size()?card(items.get(start+col),selection):new View(this),lp);
            }
            content.addView(row);
        }
    }
    private void appShelf(List<ResolveInfo> items, Set<String> fav) {
        if(items.isEmpty()) return;
        HorizontalScrollView shelf=new HorizontalScrollView(this); shelf.setHorizontalScrollBarEnabled(false); shelf.setClipToPadding(false);
        LinearLayout pages=new LinearLayout(this); pages.setPadding(dp(5),dp(9),dp(5),dp(12)); pages.setClipChildren(false);
        int cols=columns(), rows=prefs.getInt("home_rows",1), pageSize=cols*rows;
        int width=getResources().getConfiguration().screenWidthDp-96;
        for(int start=0;start<items.size();start+=pageSize) {
            LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setClipChildren(false);
            for(int r=0;r<rows && start+r*cols<items.size();r++) {
                LinearLayout row=new LinearLayout(this); row.setClipChildren(false); row.setPadding(0,dp(6),0,dp(6));
                for(int c=0;c<cols;c++) {
                    int index=start+r*cols+c;
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(tileHeight()),1); lp.setMargins(dp(5),0,dp(5),0);
                    row.addView(index<items.size()?card(items.get(index),fav):new View(this),lp);
                }
                page.addView(row);
            }
            pages.addView(page,new LinearLayout.LayoutParams(dp(width),-2));
        }
        shelf.addView(pages); content.addView(shelf);
    }
    private void addNav(LinearLayout row, View v) { LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2,-2); lp.rightMargin=dp(12); row.addView(v,lp); }
    private View card(ResolveInfo app, Set<String> fav) {
        String pkg = app.activityInfo.packageName;
        appColors.put(pkg, colorFor(app));
        LinearLayout tile = new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(9),dp(8),dp(9),dp(8));
        ImageView icon = new ImageView(this); android.graphics.drawable.Drawable banner = app.activityInfo.loadBanner(getPackageManager());
        if (banner == null) banner = app.activityInfo.applicationInfo.loadBanner(getPackageManager());
        icon.setImageDrawable(banner != null ? banner : app.loadIcon(getPackageManager()));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER); icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(icon,new LinearLayout.LayoutParams(banner != null ? -1 : dp(tileHeight()*.48f),dp(tileHeight()*.57f)));
        String name = app.loadLabel(getPackageManager()).toString();
        TextView label = text(name + (allApps && hiddenApps().contains(pkg) ? " · Masquée" : ""), 13, Color.WHITE); label.setGravity(Gravity.CENTER); label.setMaxLines(1); label.setEllipsize(android.text.TextUtils.TruncateAt.END); label.setPadding(0,dp(8),0,0); tile.addView(label);
        tile.setContentDescription(name + (fav.contains(pkg) ? ", mise en avant" : "") + (hiddenApps().contains(pkg) ? ", masquée de l’accueil" : "")); focusable(tile,pkg);
        tile.setOnClickListener(v -> safeStart(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER).setComponent(new ComponentName(pkg,app.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        tile.setOnLongClickListener(v -> { appOptions(app); return true; });
        return tile;
    }
    private int columns() {
        int desired=new int[]{6,5,4}[prefs.getInt("tile_size",1)];
        return Math.max(2,Math.min(desired,(getResources().getConfiguration().screenWidthDp-86)/120));
    }
    private int tileHeight() { return new int[]{96,116,140}[prefs.getInt("tile_size",1)]; }
    private int haloAlpha() { return new int[]{0,90,165,235}[prefs.getInt("halo",2)]; }
    private int withAlpha(int color,int alpha) { return (color & 0xffffff) | (alpha << 24); }
    private Set<String> hiddenApps() { return new HashSet<>(prefs.getStringSet("hidden_apps",Collections.emptySet())); }
    private void sortApps() {
        List<String> order=Arrays.asList(prefs.getString("app_order", "").split("\\|"));
        apps.sort((a,b)-> {
            int ai=order.indexOf(a.activityInfo.packageName),bi=order.indexOf(b.activityInfo.packageName);
            if(ai<0)ai=Integer.MAX_VALUE; if(bi<0)bi=Integer.MAX_VALUE;
            return ai!=bi?Integer.compare(ai,bi):a.loadLabel(getPackageManager()).toString().compareToIgnoreCase(b.loadLabel(getPackageManager()).toString());
        });
    }
    private void appOptions(ResolveInfo app) {
        String pkg=app.activityInfo.packageName; boolean hidden=hiddenApps().contains(pkg), selected=homeSelection().contains(pkg);
        new AlertDialog.Builder(this).setTitle(app.loadLabel(getPackageManager()))
            .setItems(new String[]{"Déplacer",hidden?"Afficher sur l’accueil":"Masquer de l’accueil", selected?"Retirer de la sélection":"Mettre en avant", "Choisir une vidéo de fond", "Utiliser le fond intégré"},(d,which)-> {
                if(which==0) {
                    movingPackage=pkg;
                    Toast.makeText(this,"Gauche / droite pour déplacer · OK pour terminer",Toast.LENGTH_LONG).show();
                } else if(which==1) {
                    Set<String> h=hiddenApps(); if(hidden)h.remove(pkg);else h.add(pkg);
                    Set<String> sel=homeSelection(); if(hidden)sel.add(pkg);
                    prefs.edit().putStringSet("hidden_apps",h).putStringSet("home_selection",sel).apply(); render();
                } else if(which==3) {chooseVideo(pkg);
                } else if(which==4) {
                    String old=prefs.getString("video_"+pkg,"");
                    prefs.edit().remove("video_"+pkg).apply(); releaseVideoPermission(old);render();
                } else {
                    Set<String> sel=homeSelection(),h=hiddenApps();
                    if(selected)sel.remove(pkg);else {sel.add(pkg);h.remove(pkg);}
                    prefs.edit().putStringSet("home_selection",sel).putStringSet("hidden_apps",h).apply(); render();
                }
            }).setNegativeButton("Fermer",null).show();
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if(movingPackage!=null) {
            int key=event.getKeyCode();
            if(key==KeyEvent.KEYCODE_BACK || key==KeyEvent.KEYCODE_DPAD_CENTER || key==KeyEvent.KEYCODE_ENTER) {
                if(event.getAction()==KeyEvent.ACTION_UP) {movingPackage=null; render();}
                return true;
            }
            if(key==KeyEvent.KEYCODE_DPAD_LEFT || key==KeyEvent.KEYCODE_DPAD_RIGHT) {
                if(event.getAction()==KeyEvent.ACTION_DOWN) moveApp(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);
                return true;
            }
            if(key==KeyEvent.KEYCODE_DPAD_UP || key==KeyEvent.KEYCODE_DPAD_DOWN)return true;
        }
        return super.dispatchKeyEvent(event);
    }
    private void moveApp(int direction) {
        Set<String> selection=homeSelection(),hidden=hiddenApps();
        ArrayList<ResolveInfo> group=new ArrayList<>();
        for(ResolveInfo app:apps) if(allApps || (!hidden.contains(app.activityInfo.packageName) && selection.contains(app.activityInfo.packageName)==selection.contains(movingPackage))) group.add(app);
        int from=-1;
        for(int i=0;i<group.size();i++)if(group.get(i).activityInfo.packageName.equals(movingPackage))from=i;
        int to=from+direction;
        if(from<0 || to<0 || to>=group.size())return;
        Collections.swap(apps,apps.indexOf(group.get(from)),apps.indexOf(group.get(to)));
        ArrayList<String> order=new ArrayList<>();for(ResolveInfo app:apps)order.add(app.activityInfo.packageName);
        prefs.edit().putString("app_order",android.text.TextUtils.join("|",order)).apply();
        restoreFocus=movingPackage; render();
    }
    private void preference(String title,String key,String[] labels,int fallback) {
        new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(labels,prefs.getInt(key,fallback),(d,which)-> {
            prefs.edit().putInt(key,which).apply();d.dismiss();render();
        }).setNegativeButton("Annuler",null).show();
    }
    private void settingsMenu() {
        new AlertDialog.Builder(this).setTitle("Personnaliser Sébé TV").setItems(new String[]{"Gérer mes applications","Taille des tuiles","Nombre de rangées mises en avant","Intensité du halo","Réglages de la télévision","Raccourcis TV"},(d,which)-> {
            if(which==0)manageApplications();
            if(which==1)preference("Taille des tuiles","tile_size",new String[]{"Petites","Moyennes","Grandes"},1);
            if(which==2)new AlertDialog.Builder(this).setTitle("Rangées mises en avant").setSingleChoiceItems(new String[]{"1 rangée","2 rangées","3 rangées"},prefs.getInt("home_rows",1)-1,(dialog,index)-> {prefs.edit().putInt("home_rows",index+1).apply();dialog.dismiss();render();}).setNegativeButton("Annuler",null).show();
            if(which==3)preference("Intensité du halo","halo",new String[]{"Désactivé","Doux","Équilibré","Intense"},2);
            if(which==4)safeStart(new Intent(Settings.ACTION_SETTINGS));
            if(which==5)shortcuts();
        }).setNegativeButton("Fermer",null).show();
    }
    private void ambianceMenu() {
        new AlertDialog.Builder(this).setTitle("Ambiance").setItems(new String[]{"Choisir le thème","Intensité du halo","Ambiance plein écran",prefs.getBoolean("motion",true)?"Désactiver les fonds animés":"Activer les fonds animés"},(d,which)-> {
            if(which==0)chooseTheme();
            if(which==1)preference("Intensité du halo","halo",new String[]{"Désactivé","Doux","Équilibré","Intense"},2);
            if(which==2)ambient();
            if(which==3){prefs.edit().putBoolean("motion",!prefs.getBoolean("motion",true)).apply();render();}
        }).setNegativeButton("Fermer",null).show();
    }
    private void ambient() {
        if(backgroundVideo!=null)backgroundVideo.stop();
        if(landscape!=null)landscape.setMotion(false);
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar_Fullscreen);
        dialog.setOnDismissListener(d->selectBackground(restoreFocus));
        Landscape background=new Landscape(this); background.setFocusable(true); background.setClickable(true);
        background.setOnClickListener(v->dialog.dismiss());
        dialog.setContentView(background); dialog.show();
        dialog.getWindow().setLayout(-1,-1);
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        background.requestFocus();
        Toast.makeText(this,"OK ou Retour pour quitter l’ambiance",Toast.LENGTH_SHORT).show();
    }
    private void shortcuts() {
        new AlertDialog.Builder(this).setTitle("Raccourcis TV").setItems(new String[]{"Entrées HDMI","Réglages TV","Ambiance plein écran"},(d,which)-> {
            if(which==0)inputs();if(which==1)safeStart(new Intent(Settings.ACTION_SETTINGS));if(which==2)ambient();
        }).setNegativeButton("Fermer",null).show();
    }
    private void inputs() {
        android.media.tv.TvInputManager manager=(android.media.tv.TvInputManager)getSystemService(Context.TV_INPUT_SERVICE);
        ArrayList<android.media.tv.TvInputInfo> inputs=new ArrayList<>(); ArrayList<String> labels=new ArrayList<>();
        try {
            if(manager!=null)for(android.media.tv.TvInputInfo input:manager.getTvInputList())if(input.isPassthroughInput()) {
                inputs.add(input); labels.add(input.loadLabel(this).toString());
            }
        } catch(SecurityException ignored) { }
        if(inputs.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Entrées TV").setMessage("Cette télévision ne partage pas ses entrées avec Sébé TV. Utilisez la touche Source / Input de la télécommande.").setPositiveButton("Réglages TV",(d,w)->safeStart(new Intent(Settings.ACTION_SETTINGS))).setNegativeButton("Fermer",null).show();return;
        }
        new AlertDialog.Builder(this).setTitle("Entrées TV / HDMI").setItems(labels.toArray(new String[0]),(d,which)->safeStart(new Intent(Intent.ACTION_VIEW,android.media.tv.TvContract.buildChannelUriForPassthroughInput(inputs.get(which).getId())))).setNegativeButton("Fermer",null).show();
    }
    private void selectBackground(String pkg) {
        if(landscape==null)return;
        String label="";
        for(ResolveInfo app:apps)if(app.activityInfo.packageName.equals(pkg)){label=app.loadLabel(getPackageManager()).toString();break;}
        landscape.brand(label);
        landscape.tintTo(appColors.getOrDefault(pkg,baseTint()));
        boolean motion=resumed && prefs.getBoolean("motion",true);
        landscape.setMotion(motion);
        if(backgroundVideo!=null)backgroundVideo.select(motion?prefs.getString("video_"+pkg,builtInVideo(pkg,label)):"");
    }
    private String builtInVideo(String pkg,String label) {
        String key=(pkg+" "+label).toLowerCase(Locale.ROOT);
        int resource=0;
        if(key.contains("netflix"))resource=R.raw.intro_netflix;
        else if(key.contains("disney"))resource=R.raw.intro_disney;
        else if(pkg.startsWith("com.apple.atve") || label.equalsIgnoreCase("Apple TV") || label.equalsIgnoreCase("Apple TV+"))resource=R.raw.intro_apple;
        else if(key.contains("crunchyroll"))resource=R.raw.intro_crunchyroll;
        else if(key.contains("canal"))resource=R.raw.intro_canal;
        else if(key.contains("youtube") && !key.contains("music"))resource=R.raw.intro_youtube;
        return resource==0?"":android.content.ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+getPackageName()+"/"+resource;
    }
    private void chooseVideo(String pkg) {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("video/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pendingVideoPackage=pkg;
        try {startActivityForResult(intent,61);}
        catch(ActivityNotFoundException e){pendingVideoPackage=null;new AlertDialog.Builder(this).setTitle("Choisir une vidéo")
            .setMessage("Aucun sélecteur de fichiers compatible n’est installé. Ajoutez un gestionnaire de fichiers compatible avec le sélecteur Android, puis choisissez votre vidéo depuis le stockage de la TV ou une clé USB.").setPositiveButton("OK",null).show();}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=61)return;
        String pkg=pendingVideoPackage;pendingVideoPackage=null;
        if(result!=RESULT_OK || data==null || data.getData()==null || pkg==null)return;
        android.net.Uri uri=data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String old=prefs.getString("video_"+pkg,"");
            prefs.edit().putString("video_"+pkg,uri.toString()).apply();
            if(!old.equals(uri.toString()))releaseVideoPermission(old);
            restoreFocus=pkg;
            Toast.makeText(this,"Vidéo associée · lecture en boucle sans son",Toast.LENGTH_LONG).show();
        } catch(SecurityException e) {Toast.makeText(this,"Impossible de conserver l’accès à cette vidéo. Choisissez un autre fichier.",Toast.LENGTH_LONG).show();}
    }
    private void releaseVideoPermission(String uri) {
        if(uri.isEmpty())return;
        for(Map.Entry<String,?> entry:prefs.getAll().entrySet())if(entry.getKey().startsWith("video_") && uri.equals(entry.getValue()))return;
        try{getContentResolver().releasePersistableUriPermission(android.net.Uri.parse(uri),Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(SecurityException ignored){}
    }
    private int baseTint() { return new int[]{0xff604b35,0xff354c84,0xff2c675e}[theme]; }
    private int colorFor(ResolveInfo app) {
        String key=(app.activityInfo.packageName+" "+app.loadLabel(getPackageManager())).toLowerCase(Locale.ROOT);
        if(key.contains("netflix")) return 0xffe50914;
        if(key.contains("youtube")) return 0xffff2929;
        if(key.contains("disney")) return 0xff087de5;
        if(key.contains("prime") || key.contains("amazonvideo")) return 0xff00a8e1;
        if(key.contains("crunchyroll")) return 0xffff861b;
        if(key.contains("spotify")) return 0xff1ed760;
        if(key.contains("twitch")) return 0xff9146ff;
        if(key.contains("apple")) return 0xffb9c9dc;
        if(key.contains("canal")) return 0xff8896ac;
        if(key.contains("plex")) return 0xffe5a00d;
        if(key.contains("vlc")) return 0xffff8c21;
        if(key.contains("arte")) return 0xffff6033;
        return accents[theme];
    }
    private void chooseTheme() {
        new AlertDialog.Builder(this).setTitle("Votre ambiance").setSingleChoiceItems(new String[]{"Cinéma · ambre", "Minuit · bleu", "Nature · sauge"},theme,(dialog,which) -> {
            prefs.edit().putInt("theme",which).apply(); dialog.dismiss(); restoreFocus="theme"; render();
        }).setNegativeButton("Fermer",null).show();
    }
    private void safeStart(Intent intent) {
        try { startActivity(intent); } catch (ActivityNotFoundException | SecurityException e) { Toast.makeText(this,"Cette application n’est pas accessible sur cette télé.",Toast.LENGTH_LONG).show(); }
    }
    @Override public void onBackPressed() {
        if (allApps) { allApps=false; restoreFocus="home"; render(); }
        else new AlertDialog.Builder(this).setTitle("Quitter Sébé TV ?").setMessage("Revenir à l’écran précédent.").setPositiveButton("Quitter",(d,w)->finish()).setNegativeButton("Rester",null).show();
    }
    private class Landscape extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path[] ridges={new Path(),new Path(),new Path()};
        private final int[] ridgeColors={0x244a526b,0x333d4963,0x88101825};
        private Shader sky,glow,veil;
        private int tint = baseTint();
        private android.animation.ValueAnimator tintAnimation;
        private String brandLabel="",brandKey="";
        private boolean motion;
        private long started;
        private final Paint cinematic=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF arc=new RectF();
        private final Typeface cinemaFont=Typeface.create("sans-serif-light",Typeface.NORMAL);
        void brand(String label) {brandLabel=label;brandKey=label.toLowerCase(Locale.ROOT);started=SystemClock.uptimeMillis();invalidate();}
        void setMotion(boolean enabled) {motion=enabled;invalidate();}
        Landscape(Context c) { super(c); setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO); }
        void tintTo(int target) {
            if (tintAnimation != null) tintAnimation.cancel();
            if (target == tint) return;
            if (getWidth()==0) { tint=target; return; }
            tintAnimation = android.animation.ValueAnimator.ofArgb(tint,target);
            tintAnimation.setDuration(420);
            tintAnimation.addUpdateListener(animation -> { tint=(Integer)animation.getAnimatedValue(); updateGlow(); invalidate(); });
            tintAnimation.start();
        }
        private void updateGlow() {
            if(getWidth()>0) glow=new RadialGradient(getWidth()*.62f,getHeight()*.3f,getWidth()*1.05f,
                new int[]{withAlpha(tint,haloAlpha()), withAlpha(tint,haloAlpha()/2),0x00151e30},new float[]{0f,.52f,1f},Shader.TileMode.CLAMP);
        }
        @Override protected void onDetachedFromWindow() {
            motion=false;
            if(tintAnimation!=null) tintAnimation.cancel();
            super.onDetachedFromWindow();
        }
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
            sky=new LinearGradient(0,0,w,h,0xff090e19,0xff151e30,Shader.TileMode.CLAMP);
            updateGlow();
            veil=new LinearGradient(0,0,0,h,new int[]{0x11060b15,0x44060b15,0xf2080d17},null,Shader.TileMode.CLAMP);
            for(int i=0;i<3;i++) {
                Path p=ridges[i]; p.reset(); p.moveTo(0,h); p.lineTo(0,h*(.79f+i*.04f));
                p.cubicTo(w*.2f,h*(.55f+i*.1f),w*.35f,h*(.98f-i*.05f),w*.57f,h*(.64f+i*.08f));
                p.cubicTo(w*.75f,h*(.41f+i*.12f),w*.82f,h*(.65f+i*.09f),w,h*(.44f+i*.15f)); p.lineTo(w,h); p.close();
            }
        }
        @Override protected void onDraw(Canvas canvas) {
            float w=getWidth(),h=getHeight();
            paint.setAlpha(255);paint.setShader(sky); canvas.drawRect(0,0,w,h,paint);
            paint.setShader(glow); canvas.drawRect(0,0,w,h,paint); paint.setShader(null);
            for(int i=0;i<3;i++) { paint.setColor(ridgeColors[i]); canvas.drawPath(ridges[i],paint); }
            paint.setAlpha(255);paint.setShader(veil); canvas.drawRect(0,0,w,h,paint); paint.setShader(null);
            if(!brandLabel.isEmpty())drawCinema(canvas,w,h);
            if(motion && !brandLabel.isEmpty() && isShown())postInvalidateDelayed(33);
        }
        private void drawCinema(Canvas canvas,float w,float h) {
            float phase=motion?((SystemClock.uptimeMillis()-started)%9000)/9000f:0.3f;
            int alpha=Math.max(35,haloAlpha());
            cinematic.setStyle(Paint.Style.FILL);
            if(brandKey.contains("netflix")) {
                // Original flowing red light curtains, not an embedded official intro.
                for(int i=0;i<28;i++) {
                    float x=w*(.18f+i*.026f)+w*.025f*(float)Math.sin(phase*Math.PI*2+i*.35f);
                    cinematic.setColor(withAlpha(0xffe50914,(int)(alpha*(.12f+.24f*(1+Math.sin(i+phase*6.28f))/2))));
                    canvas.drawRect(x,h*.12f,x+w*(.003f+(i%3)*.002f),h,cinematic);
                }
            } else if(brandKey.contains("disney")) {
                cinematic.setStyle(Paint.Style.STROKE);cinematic.setStrokeWidth(dp(2));cinematic.setColor(withAlpha(0xffa9dfff,alpha));
                arc.set(w*.30f,h*.45f,w*.86f,h*1.05f);canvas.drawArc(arc,195,150,false,cinematic);
                double angle=Math.toRadians(195+phase*150);float x=arc.centerX()+(float)Math.cos(angle)*arc.width()/2,y=arc.centerY()+(float)Math.sin(angle)*arc.height()/2;
                cinematic.setStyle(Paint.Style.FILL);cinematic.setColor(0xffe5f5ff);canvas.drawCircle(x,y,dp(3),cinematic);
                for(int i=0;i<24;i++){float sx=w*(.15f+((i*37)%77)/100f),sy=h*(.38f+((i*19)%53)/100f);cinematic.setColor(withAlpha(0xffbce7ff,(int)(alpha*(.3f+.35f*(1+Math.sin(i+phase*6.28f))/2))));canvas.drawCircle(sx,sy,dp(i%3==0?1.5f:.8f),cinematic);}
            } else {
                cinematic.setStyle(Paint.Style.STROKE);cinematic.setStrokeWidth(dp(2));
                for(int i=0;i<4;i++){cinematic.setColor(withAlpha(tint,alpha/(3+i)));float radius=w*(.12f+i*.07f+phase*.05f);canvas.drawCircle(w*.70f,h*.74f,radius,cinematic);}
            }
            cinematic.setStyle(Paint.Style.FILL);cinematic.setTypeface(cinemaFont);
            cinematic.setTextSize(Math.min(w*.075f,dp(66)));cinematic.setTextAlign(Paint.Align.CENTER);
            cinematic.setColor(withAlpha(0xffeaf1ff,110));
            String name=brandKey.contains("netflix")?"NETFLIX":brandLabel;
            float textWidth=cinematic.measureText(name);if(textWidth>w*.72f)cinematic.setTextSize(cinematic.getTextSize()*w*.72f/textWidth);
            canvas.drawText(name,w*.58f,h*.80f,cinematic);
        }
    }
}
