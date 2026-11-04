package it.jonica.app.data

import it.jonica.app.data.Category.DISPENSA
import it.jonica.app.data.Category.FRUTTA
import it.jonica.app.data.Category.MARE
import it.jonica.app.data.Category.VERDURA

private fun m(vararg months: Int) = months.toSet()
private fun range(from: Int, to: Int): Set<Int> =
    if (from <= to) (from..to).toSet() else ((from..12) + (1..to)).toSet()

// Calendario indicativo per il Centro-Sud Italia (pieno campo, senza serra).
val products: List<Product> = listOf(
    // ── FRUTTA ─────────────────────────────────────────────
    Product("Arance", "🍊", FRUTTA, range(11, 4), "Pesanti in mano = succose. La buccia un po' irregolare è segno di frutto non trattato in cera.", "Sicilia, Calabria"),
    Product("Mandarini", "🍊", FRUTTA, range(11, 2), "Scegli quelli con la foglia: sono stati raccolti da poco.", "Calabria, Sicilia"),
    Product("Clementine", "🍊", FRUTTA, range(11, 2), "La Clementina di Calabria IGP è a pochi giorni di camion da Roma.", "Calabria"),
    Product("Limoni", "🍋", FRUTTA, range(11, 5), "Buccia liscia e sottile = più succo. Se non trattati, usa anche la scorza.", "Sicilia, Amalfi"),
    Product("Pompelmi", "🍊", FRUTTA, range(12, 4), "Ottimi a colazione, si conservano a lungo fuori dal frigo.", "Sicilia"),
    Product("Kiwi", "🥝", FRUTTA, range(11, 4), "Il Lazio è tra i primi produttori al mondo: il Kiwi Latina IGP è a km quasi zero.", "Agro Pontino"),
    Product("Mele", "🍎", FRUTTA, range(9, 3), "Si conservano mesi in luogo fresco: comprale in cassetta dal produttore.", "Trentino, Lazio"),
    Product("Pere", "🍐", FRUTTA, range(8, 1), "Comprale ancora sode e lasciale maturare a temperatura ambiente."),
    Product("Cachi", "🟠", FRUTTA, m(10, 11, 12), "Maturano in casa vicino alle mele. Il cachi-mela si mangia sodo."),
    Product("Melograni", "🔴", FRUTTA, m(10, 11, 12), "Buccia tesa e pesante: i chicchi sono pieni. Ricchissimi di antiossidanti."),
    Product("Castagne", "🌰", FRUTTA, m(10, 11, 12), "Lucide e senza forellini. Quelle dei Monti Cimini (Viterbo) sono eccellenti.", "Viterbo"),
    Product("Uva", "🍇", FRUTTA, m(8, 9, 10), "Il grappolo deve avere il raspo verde, non secco."),
    Product("Fichi", "🟤", FRUTTA, m(7, 8, 9), "Delicatissimi: comprali solo il giorno in cui li mangi."),
    Product("Fichi d'India", "🌵", FRUTTA, m(8, 9, 10), "Fatteli pulire dal fruttivendolo o usa i guanti: le spine sono insidiose.", "Sicilia"),
    Product("Mele cotogne", "🍏", FRUTTA, m(10, 11), "Crude sono aspre, ma diventano la migliore cotognata."),
    Product("Fragole", "🍓", FRUTTA, m(4, 5, 6), "Le fragole a gennaio vengono da serre riscaldate o da molto lontano: aspetta aprile."),
    Product("Nespole", "🟡", FRUTTA, m(5, 6), "Durano pochissimo: un frutto antico che si trova quasi solo nei piccoli negozi."),
    Product("Ciliegie", "🍒", FRUTTA, m(5, 6, 7), "Picciolo verde e attaccato = appena raccolte."),
    Product("Albicocche", "🍑", FRUTTA, m(6, 7), "Profumate e cedevoli al tatto: se sono dure non matureranno davvero."),
    Product("Pesche", "🍑", FRUTTA, m(6, 7, 8, 9), "Il profumo è il miglior indicatore. Evita quelle con ammaccature."),
    Product("Susine", "🟣", FRUTTA, m(6, 7, 8, 9), "La patina bianca (pruina) è naturale e protegge il frutto."),
    Product("Meloni", "🍈", FRUTTA, m(6, 7, 8), "Annusa il punto del picciolo: deve profumare intensamente."),
    Product("Anguria", "🍉", FRUTTA, m(6, 7, 8), "Bussa: suono sordo e macchia gialla sotto = matura.", "Lazio, Puglia"),
    Product("Mirtilli", "🫐", FRUTTA, m(7, 8, 9), "Si congelano benissimo: fai scorta quando costano meno."),
    Product("Lamponi", "🍓", FRUTTA, m(6, 7, 8, 9), "Fragilissimi: controlla il fondo della vaschetta."),
    Product("Noci", "🥜", FRUTTA, m(9, 10, 11), "Le noci italiane costano di più ma non viaggiano dall'altra parte del mondo."),
    Product("Nocciole", "🌰", FRUTTA, m(8, 9, 10), "La Nocciola Romana DOP viene dai Monti Cimini, a un'ora da Roma.", "Viterbo"),

    // ── VERDURA ────────────────────────────────────────────
    Product("Carciofo romanesco", "🌱", VERDURA, range(1, 5) + m(11, 12), "Il Carciofo Romanesco IGP è al massimo tra febbraio e aprile: foglie serrate e gambo sodo.", "Lazio"),
    Product("Puntarelle", "🥬", VERDURA, range(11, 2), "Regina dell'inverno romano: in molte botteghe te le vendono già tagliate.", "Lazio"),
    Product("Broccoletti", "🥦", VERDURA, range(10, 3), "Le cime di rapa romane: si cuociono in padella con aglio e peperoncino."),
    Product("Broccolo romanesco", "🥦", VERDURA, range(10, 3), "Le sue spirali verdi sono un frattale naturale: più è compatto, più è fresco.", "Lazio"),
    Product("Cavolfiore", "🥦", VERDURA, range(10, 3), "Testa bianca, compatta, con le foglie verdi ancora fresche."),
    Product("Cavolo nero", "🥬", VERDURA, range(10, 2), "Dà il meglio dopo le prime gelate, quando diventa più dolce."),
    Product("Verza", "🥬", VERDURA, range(10, 3), "Una verza intera dura settimane in frigo: economica e versatile."),
    Product("Cavoletti di Bruxelles", "🥬", VERDURA, range(10, 2), "Piccoli e chiusi sono più dolci."),
    Product("Cicoria", "🥬", VERDURA, range(10, 4), "Ripassata in padella è un classico romano. Quella di campo è più amara."),
    Product("Finocchi", "🧅", VERDURA, range(10, 4), "Tondi (maschi) per crudi, allungati (femmine) per cotture."),
    Product("Radicchio", "🥬", VERDURA, range(10, 3), "Il tardivo arriva da dicembre: sapore amarognolo e croccante."),
    Product("Spinaci", "🥬", VERDURA, range(10, 4), "Foglie piccole per l'insalata, grandi per la cottura."),
    Product("Bietole", "🥬", VERDURA, range(9, 5), "Usa anche le coste: sono buonissime gratinate."),
    Product("Porri", "🧅", VERDURA, range(10, 3), "La parte verde è perfetta per il brodo: niente sprechi."),
    Product("Zucca", "🎃", VERDURA, range(9, 1), "Intera si conserva per mesi in un luogo fresco e asciutto."),
    Product("Topinambur", "🥔", VERDURA, range(11, 2), "Un tubero dimenticato, dolce come il carciofo. Si trova nei mercati rionali."),
    Product("Sedano", "🌿", VERDURA, range(9, 3), "Le foglie si usano nei soffritti e nelle zuppe."),
    Product("Rape", "🟣", VERDURA, range(10, 2), "Ottime arrosto o in zuppa."),
    Product("Funghi porcini", "🍄", VERDURA, m(9, 10, 11), "Compra solo funghi certificati dall'ispettorato micologico."),
    Product("Carote", "🥕", VERDURA, range(1, 12), "Quelle col ciuffo sono più fresche, ma toglilo a casa così durano di più."),
    Product("Asparagi", "🌱", VERDURA, m(3, 4, 5), "Punte chiuse e gambo che si spezza netto. Quelli selvatici sono un tesoro primaverile."),
    Product("Agretti", "🌿", VERDURA, m(3, 4, 5), "Detti 'barba di frate', durano poche settimane: approfittane."),
    Product("Fave", "🫛", VERDURA, m(4, 5, 6), "Il 1° maggio a Roma si mangiano crude con il pecorino."),
    Product("Piselli", "🫛", VERDURA, m(4, 5, 6), "Baccelli verdi brillanti e turgidi: dentro sono dolcissimi."),
    Product("Ravanelli", "🔴", VERDURA, m(3, 4, 5, 6), "Le foglie si saltano in padella come gli spinaci."),
    Product("Lattuga", "🥬", VERDURA, range(4, 10), "Comprala intera: le buste pronte costano fino a 5 volte di più al kg."),
    Product("Rucola", "🌿", VERDURA, range(3, 11), "Quella selvatica, più piccante, è tipica dei mercati."),
    Product("Zucchine", "🥒", VERDURA, range(5, 9), "Piccole, sode e con il fiore: le più saporite."),
    Product("Fiori di zucca", "🌼", VERDURA, range(5, 8), "Da usare in giornata: fritti alla romana con alice e mozzarella."),
    Product("Pomodori", "🍅", VERDURA, range(6, 9), "Un pomodoro a dicembre ha viaggiato o è cresciuto in serra: in estate costa meno e sa di più."),
    Product("Melanzane", "🍆", VERDURA, range(6, 9), "Buccia lucida e tesa, picciolo verde."),
    Product("Peperoni", "🫑", VERDURA, range(6, 9), "I rossi e gialli sono solo verdi più maturi: più dolci e vitaminici."),
    Product("Cetrioli", "🥒", VERDURA, m(6, 7, 8), "Freschissimi d'estate, idratano più di molte bevande."),
    Product("Fagiolini", "🫛", VERDURA, range(6, 9), "Devono spezzarsi con uno schiocco."),
    Product("Patate", "🥔", VERDURA, range(5, 11), "Le novelle arrivano a maggio: buccia sottile, non serve pelarle."),
    Product("Cipolle", "🧅", VERDURA, range(5, 9), "Le cipolle si conservano tutto l'anno, ma quelle fresche arrivano in estate."),
    Product("Aglio", "🧄", VERDURA, m(5, 6, 7), "L'aglio fresco ha un sapore più delicato. L'aglio italiano è facilmente distinguibile dall'etichetta."),

    // ── MARE (Tirreno) ─────────────────────────────────────
    Product("Alici", "🐟", MARE, range(4, 9), "Occhio vivo e corpo rigido: costano poco e sono uno dei pesci più sostenibili."),
    Product("Sardine", "🐟", MARE, range(5, 9), "Pesce azzurro ricco di Omega-3, economico e del nostro mare."),
    Product("Sgombro", "🐟", MARE, range(3, 7), "Ottimo alla griglia. Un'alternativa sostenibile al tonno."),
    Product("Pesce spada", "🗡️", MARE, range(5, 8), "Chiedi da dove arriva: quello del Mediterraneo si pesca in estate."),
    Product("Triglie", "🐠", MARE, m(9, 10, 11), "Rosse e brillanti: in autunno sono al meglio."),
    Product("Cefalo", "🐟", MARE, range(9, 12), "Pesce povero e saporito, poco considerato: costa poco."),
    Product("Seppie", "🦑", MARE, range(2, 5), "In primavera si avvicinano alla costa: perfette con i piselli."),
    Product("Calamari", "🦑", MARE, range(10, 2), "Quelli locali sono più piccoli e saporiti di quelli surgelati d'importazione."),
    Product("Polpo", "🐙", MARE, range(10, 4), "Rispetta le taglie minime: i polpi piccoli non hanno ancora potuto riprodursi."),
    Product("Spigola", "🐟", MARE, range(12, 3), "Quella selvatica è invernale; il resto dell'anno è quasi sempre allevata."),
    Product("Nasello", "🐟", MARE, range(10, 3), "Delicato, ottimo per i bambini."),
    Product("Vongole", "🐚", MARE, range(3, 5) + range(9, 11), "Devono essere chiuse o chiudersi se toccate."),

    // ── DISPENSA E ALLEVAMENTO ─────────────────────────────
    Product("Olio nuovo", "🫒", DISPENSA, m(10, 11, 12, 1), "Appena franto è verde, amaro e piccante: segno di polifenoli. Compralo al frantoio.", "Sabina, Tuscia"),
    Product("Ricotta di pecora", "🥛", DISPENSA, range(11, 5), "La Ricotta Romana DOP è più buona quando le pecore pascolano erba fresca.", "Lazio"),
    Product("Pecorino romano", "🧀", DISPENSA, range(1, 12), "Stagionato almeno 5 mesi: compralo al taglio, non grattugiato in busta."),
    Product("Abbacchio", "🐑", DISPENSA, range(12, 4), "L'Abbacchio Romano IGP è legato a Natale e Pasqua: chiedi l'allevamento al macellaio.", "Lazio"),
    Product("Miele", "🍯", DISPENSA, range(4, 9), "Si produce in primavera-estate. Il miele italiano costa di più ma non è mescolato con miele estero."),
    Product("Vino novello", "🍷", DISPENSA, m(11), "Esce il 30 ottobre: fruttato, da bere giovane."),
    Product("Tartufo bianco", "🤍", DISPENSA, range(10, 12), "Si raccoglie in autunno. Anche il Lazio ha le sue tartufaie."),
    Product("Tartufo nero", "🖤", DISPENSA, range(12, 3), "Il nero pregiato è invernale; d'estate c'è lo scorzone, più economico."),
    Product("Legumi secchi nuovi", "🫘", DISPENSA, range(8, 11), "Lenticchie e ceci del raccolto estivo cuociono prima e sono più saporiti."),
    Product("Uova", "🥚", DISPENSA, range(1, 12), "Leggi il primo numero del codice: 0 bio, 1 all'aperto, 2 a terra, 3 in gabbia."),
)

val farmEvents: List<FarmEvent> = listOf(
    FarmEvent(
        "Olio nuovo al frantoio", "🫒", m(10, 11, 12),
        "Da ottobre a dicembre si raccolgono le olive e i frantoi lavorano giorno e notte. L'olio appena franto è verde, piccante e amaro: è ricchissimo di polifenoli.",
        "Il momento migliore per comprare la scorta annuale è tra novembre e gennaio, direttamente dal frantoio o da una bottega che lo conosce. Conservalo al buio e al fresco, in latta o vetro scuro, e consumalo entro 12-18 mesi.",
    ),
    FarmEvent(
        "Castagne e marroni", "🌰", m(10, 11),
        "Nei boschi dei Monti Cimini e della Tuscia si raccolgono le castagne: tempo di caldarroste e sagre.",
        "Comprale da chi le raccoglie: le castagne italiane sono minacciate dall'importazione a basso costo.",
    ),
    FarmEvent(
        "Vino novello", "🍷", m(11),
        "Dal 30 ottobre è in vendita il vino novello, prodotto con macerazione carbonica.",
        "Chiedi in enoteca un novello laziale: va bevuto giovane, entro la primavera.",
    ),
    FarmEvent(
        "Agrumi del Sud", "🍊", m(12, 1, 2, 3),
        "È la stagione piena degli agrumi di Sicilia e Calabria. Spesso agli agricoltori vengono pagati pochi centesimi al chilo, mentre al supermercato costano molto di più.",
        "Comprare agrumi italiani in bottega, o in cassetta dai gruppi d'acquisto, sostiene direttamente chi li coltiva.",
    ),
    FarmEvent(
        "Kiwi dell'Agro Pontino", "🥝", m(11, 12, 1, 2, 3),
        "Il Lazio è uno dei maggiori produttori di kiwi al mondo. In questo periodo arrivano freschi da Latina.",
        "Prima di comprare kiwi neozelandesi o cileni, guarda l'etichetta: quelli laziali sono a meno di 100 km.",
    ),
    FarmEvent(
        "Ricotta e pecorino", "🐑", m(12, 1, 2, 3, 4, 5),
        "Dall'inverno alla primavera le pecore del Lazio danno il latte migliore: è la stagione della Ricotta Romana DOP.",
        "Al caseificio o in gastronomia chiedi la ricotta del giorno: dura pochi giorni, va comprata fresca.",
    ),
    FarmEvent(
        "Carciofo romanesco", "🌱", m(2, 3, 4),
        "Il Carciofo Romanesco IGP raggiunge il massimo della qualità. Ad aprile c'è la storica sagra di Ladispoli.",
        "I mammole (cimaroli) sono i più pregiati: chiedi al fruttivendolo quelli del litorale laziale.",
    ),
    FarmEvent(
        "Asparagi e verdure di primavera", "🌱", m(3, 4, 5),
        "Asparagi, agretti, fave e piselli: la primavera dura poche settimane per ogni prodotto.",
        "Sono verdure che 'scappano': se le vedi in bottega, prendile subito.",
    ),
    FarmEvent(
        "Smielatura", "🍯", m(5, 6, 7, 8, 9),
        "Gli apicoltori raccolgono acacia, millefiori, castagno e sulla. Il clima sta rendendo le annate sempre più difficili.",
        "Compra miele da un apicoltore locale: in etichetta deve esserci 'Italia', non 'miscela di mieli UE e non UE'.",
    ),
    FarmEvent(
        "Mietitura del grano", "🌾", m(6, 7),
        "Nei campi si raccoglie il grano duro e tenero. Il prezzo pagato agli agricoltori è spesso inferiore ai costi di produzione.",
        "Scegli pasta e farine con grano 100% italiano dichiarato in etichetta.",
    ),
    FarmEvent(
        "Pomodori e passata", "🍅", m(7, 8, 9),
        "È il momento della 'giornata della conserva': i pomodori costano meno e sono al massimo del sapore.",
        "Diffida dei prezzi troppo bassi: dietro possono esserci sfruttamento e caporalato. Cerca marchi di filiera etica.",
    ),
    FarmEvent(
        "Vendemmia", "🍇", m(8, 9, 10),
        "Dai Castelli Romani alla Tuscia si raccoglie l'uva. Molte cantine aprono le porte ai visitatori.",
        "Ottobre è il mese delle cantine aperte: compra vino sfuso o in bottiglia direttamente dal produttore.",
    ),
    FarmEvent(
        "Fermo pesca", "⚓", m(8, 9, 10),
        "Per alcune settimane le flotte si fermano per lasciare che il mare si ripopoli. Le date cambiano ogni anno e da zona a zona.",
        "In questo periodo preferisci pesce azzurro di piccola pesca o allevato in Italia.",
    ),
    FarmEvent(
        "Funghi e tartufi", "🍄", m(9, 10, 11, 12),
        "I boschi del Lazio e dell'Appennino offrono porcini, ovoli e tartufi.",
        "Solo funghi controllati: le botteghe e i mercati con certificato micologico sono la scelta sicura.",
    ),
)

/** Curiosità e fatti per consapevolizzare. */
val facts: List<Pair<String, String>> = listOf(
    "🚚" to "Un prodotto fuori stagione può percorrere migliaia di km prima di arrivare nel tuo piatto. Con il gasolio alle stelle, quel viaggio lo paghi tu.",
    "💶" to "Secondo le stime ISMEA, per ogni 100 € spesi in prodotti agricoli freschi solo una ventina arriva all'agricoltore.",
    "🔥" to "Coltivare un pomodoro d'inverno in serra riscaldata può richiedere molte volte l'energia di uno estivo in pieno campo.",
    "🏪" to "Una bottega di quartiere reinveste gran parte di ciò che incassa nella stessa zona: stipendi, fornitori, servizi.",
    "🗑️" to "In Italia si sprecano decine di chili di cibo a persona ogni anno. Comprare poco e spesso, in bottega, aiuta a sprecare meno.",
    "🥚" to "Il codice sulle uova parla: il primo numero indica l'allevamento (0 bio, 1 aperto, 2 a terra, 3 gabbia), poi c'è il Paese.",
    "🫒" to "L'olio extravergine 'comunitario' a pochi euro al litro difficilmente remunera il lavoro di un olivicoltore italiano.",
    "🌾" to "Le proteste di trasportatori e agricoltori nascono anche da un divario: prezzi bassi all'origine, prezzi alti allo scaffale.",
    "🧺" to "I mercati rionali di Roma sono più di cento: molti banchi vendono direttamente ciò che producono.",
    "🍅" to "Un prodotto di stagione è più ricco di vitamine perché matura sulla pianta, non durante il trasporto.",
)
