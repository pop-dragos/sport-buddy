// The English version of the "Terms of Use" and "Privacy Policy" pages (the Romanian one is in legalContent.js).
// Keep both versions in sync: same sections, same order.

export const UPDATED = 'November 24, 2026'

export const TERMS = {
  title: 'Terms of Use',
  intro: 'SportBuddy is a project built for the Software Engineering course at the Technical University of Cluj-Napoca. '
    + 'By using the app you agree to the rules below.',
  sections: [
    {
      title: 'Your account',
      body: [
        'To have an account you must be at least 16 years old and use an email address that belongs to you.',
        'You are responsible for what happens in your account. Keep your password to yourself; if you suspect '
          + 'someone else has found out, change it in Settings or reset it from the sign-in page.',
        'You can delete your account at any time, from Settings.'
      ]
    },
    {
      title: 'Lobbies and matches',
      body: [
        'The organizer of a lobby sets the sport, the place, the time and the cost of the field. The cost is split between the participants.',
        'If you leave a match late or do not show up, the other participants can leave a review that affects your rating.',
        'SportBuddy does not manage the fields and is not responsible for what happens at matches. '
          + 'Play responsibly and take your own health into account.'
      ]
    },
    {
      title: 'Payments',
      body: [
        'In this version, online payments are simulated: no real money is charged and you do not enter card details.'
      ]
    },
    {
      title: 'Behavior',
      body: [
        'The following are not allowed:',
        [
          'offensive, discriminatory or misleading messages, posts or reviews;',
          'fake accounts or using someone else\'s data;',
          'sending spam or trying to break the app.'
        ],
        'Accounts that break these rules may be suspended.'
      ]
    },
    {
      title: 'Availability',
      body: [
        'Since this is a university project, the app is provided "as is", with no guarantee that it works without '
          + 'interruptions or errors. We may change or stop the service and may update these terms; '
          + 'the date of the last change is shown at the top of the page.'
      ]
    },
    {
      title: 'Contact',
      body: ['For any question, reply to any of the emails you received from SportBuddy.']
    }
  ]
}

export const PRIVACY = {
  title: 'Privacy Policy',
  intro: 'Here you can find out what data SportBuddy keeps about you, why, and how you can control it.',
  sections: [
    {
      title: 'What data we keep',
      body: [
        [
          'account details: name, username, email, age and, if you fill them in, phone, city, '
            + 'description and profile photo;',
          'your password, kept only in encrypted form (BCrypt hash): nobody, not even the team, can read it;',
          'your sports and declared level, the lobbies you take part in, attendance at matches, reviews and rating;',
          'posts in the feed, messages in lobby chats and your friend list;',
          'the history of simulated payments (amount, date, lobby), with no card details;',
          'your settings (theme, language, currency, notifications, privacy).'
        ]
      ]
    },
    {
      title: 'Signing in with Google',
      body: [
        'If you choose "Continue with Google", we receive from Google only your name, your email address and an '
          + 'identifier of the Google account. We have no access to your Google password, emails, files or contacts.'
      ]
    },
    {
      title: 'What we use it for',
      body: [
        [
          'to make your account and sign-in work;',
          'to show you lobbies, players and the weather for your city;',
          'to send you emails strictly related to your account: address confirmation, password reset, '
            + 'email change. We do not send ads.'
        ],
        'We do not sell or give your data to other companies.'
      ]
    },
    {
      title: 'Who can see it',
      body: [
        'Your profile (name, photo, sports, rating) is visible to other users. In Settings you can choose to make '
          + 'your profile visible to friends only, who can add you as a friend, and whether to show your friend list.',
        'Your email and phone do not appear on your public profile.'
      ]
    },
    {
      title: 'External services',
      body: [
        [
          'Google: signing in with Google and sending emails (Gmail);',
          'Open-Meteo: the weather forecast, based on the coordinates of the city (not your exact location);',
          'National Bank of Romania (BNR): the exchange rate used to show prices in EUR or USD.'
        ]
      ]
    },
    {
      title: 'What stays in your browser',
      body: [
        'We do not use tracking or advertising cookies. Your browser only keeps your sign-in session and a '
          + 'few preferences (for example, "Remember me", the theme, the language and the currency).'
      ]
    },
    {
      title: 'Deletion and your rights',
      body: [
        'When you delete your account, your profile data (name, email, phone, photo, description, city, sports, password) '
          + 'is deleted immediately. Posts, messages, reviews and match history remain, so that other people\'s lobbies and '
          + 'conversations are not broken, but they appear under the name "Deleted user", with no link to you.',
        'You can ask at any time for a copy of your data or for it to be corrected, by replying to any of the '
          + 'emails you received from SportBuddy.'
      ]
    }
  ]
}
