// Textul paginilor publice „Termeni de utilizare” și „Politica de confidențialitate”.
// Fiecare secțiune: un titlu și paragrafe; un paragraf care e listă se scrie ca array de puncte.

export const UPDATED = '24 noiembrie 2026'

export const TERMS = {
  title: 'Termeni de utilizare',
  intro: 'SportBuddy este un proiect realizat la disciplina Ingineria Software, la Universitatea Tehnică din Cluj-Napoca. '
    + 'Folosind aplicația, ești de acord cu regulile de mai jos.',
  sections: [
    {
      title: 'Contul tău',
      body: [
        'Pentru un cont trebuie să ai cel puțin 16 ani și să folosești un email care îți aparține.',
        'Ești responsabil pentru ce se întâmplă în contul tău. Păstrează parola doar pentru tine; dacă bănuiești '
          + 'că a aflat-o altcineva, schimb-o din Setări sau resetează-o din pagina de conectare.',
        'Îți poți șterge contul oricând, din Setări.'
      ]
    },
    {
      title: 'Lobby-uri și meciuri',
      body: [
        'Organizatorul unui lobby stabilește sportul, locul, ora și costul terenului. Costul se împarte între participanți.',
        'Dacă renunți târziu la un meci sau nu te prezinți, ceilalți participanți pot lăsa un review care îți afectează ratingul.',
        'SportBuddy nu administrează terenurile și nu răspunde pentru ce se întâmplă la meciuri. '
          + 'Joacă responsabil și ține cont de propria sănătate.'
      ]
    },
    {
      title: 'Plăți',
      body: [
        'În această versiune, plățile online sunt simulate: nu se percep bani reali și nu introduci date de card.'
      ]
    },
    {
      title: 'Comportament',
      body: [
        'Nu sunt permise:',
        [
          'mesajele, postările sau review-urile jignitoare, discriminatorii sau înșelătoare;',
          'conturile false sau folosirea datelor altcuiva;',
          'trimiterea de spam sau încercările de a strica funcționarea aplicației.'
        ],
        'Conturile care încalcă aceste reguli pot fi suspendate.'
      ]
    },
    {
      title: 'Disponibilitate',
      body: [
        'Fiind un proiect de facultate, aplicația este oferită „ca atare”, fără garanția că funcționează fără întreruperi '
          + 'sau erori. Putem modifica sau opri serviciul și putem actualiza acești termeni; '
          + 'data ultimei modificări apare la începutul paginii.'
      ]
    },
    {
      title: 'Contact',
      body: ['Pentru orice întrebare, răspunde la oricare dintre emailurile primite de la SportBuddy.']
    }
  ]
}

export const PRIVACY = {
  title: 'Politica de confidențialitate',
  intro: 'Aici afli ce date despre tine păstrează SportBuddy, de ce și cum le poți controla.',
  sections: [
    {
      title: 'Ce date păstrăm',
      body: [
        [
          'datele contului: nume, nume de utilizator, email, vârstă și, dacă le completezi, telefonul, orașul, '
            + 'descrierea și poza de profil;',
          'parola, păstrată doar sub formă criptată (hash BCrypt): nimeni, nici echipa, nu o poate citi;',
          'sporturile și nivelul declarat, lobby-urile la care participi, prezența la meciuri, review-urile și ratingul;',
          'postările din feed, mesajele din chat-ul lobby-urilor și lista de prieteni;',
          'istoricul plăților simulate (sumă, dată, lobby), fără date de card;',
          'setările tale (temă, notificări, confidențialitate).'
        ]
      ]
    },
    {
      title: 'Conectarea cu Google',
      body: [
        'Dacă alegi „Continuă cu Google”, primim de la Google doar numele, adresa de email și un identificator '
          + 'al contului Google. Nu avem acces la parola Google, la emailurile, fișierele sau contactele tale.'
      ]
    },
    {
      title: 'Pentru ce le folosim',
      body: [
        [
          'ca să-ți funcționeze contul și conectarea;',
          'ca să-ți arătăm lobby-uri, jucători și vremea pentru orașul tău;',
          'ca să-ți trimitem emailuri strict legate de cont: confirmarea adresei, resetarea parolei, '
            + 'schimbarea emailului. Nu trimitem reclame.'
        ],
        'Nu vindem și nu dăm datele tale altor firme.'
      ]
    },
    {
      title: 'Cine le vede',
      body: [
        'Profilul tău (nume, poză, sporturi, rating) îl văd ceilalți utilizatori. Din Setări poți alege ca profilul '
          + 'să fie vizibil doar prietenilor, cine te poate adăuga la prieteni și dacă îți afișezi lista de prieteni.',
        'Emailul și telefonul nu apar pe profilul public.'
      ]
    },
    {
      title: 'Servicii externe',
      body: [
        [
          'Google: conectarea cu Google și trimiterea emailurilor (Gmail);',
          'Open-Meteo: prognoza meteo, pe baza coordonatelor orașului (nu a locației tale exacte).'
        ]
      ]
    },
    {
      title: 'Ce rămâne în browser',
      body: [
        'Nu folosim cookie-uri de urmărire sau de reclamă. Browserul păstrează doar sesiunea ta de conectare și '
          + 'câteva preferințe (de exemplu, „Ține-mă minte” și tema).'
      ]
    },
    {
      title: 'Ștergerea și drepturile tale',
      body: [
        'Când îți ștergi contul, datele profilului (nume, email, telefon, poză, descriere, oraș, sporturi, parolă) '
          + 'sunt șterse imediat. Postările, mesajele, review-urile și istoricul meciurilor rămân, ca să nu se strice '
          + 'lobby-urile și conversațiile celorlalți, dar apar sub numele „Utilizator șters”, fără legătură cu tine.',
        'Poți cere oricând o copie a datelor tale sau corectarea lor, răspunzând la oricare dintre emailurile '
          + 'primite de la SportBuddy.'
      ]
    }
  ]
}
