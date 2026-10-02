package io.github.halilozel1903.adaptive.sample

import androidx.compose.ui.graphics.Color

enum class Folder { Inbox, Sent }

data class Attachment(val name: String, val size: String)

data class Email(
    val id: Int,
    val sender: String,
    val senderAddress: String,
    val subject: String,
    val preview: String,
    val body: List<String>,
    val time: String,
    val folder: Folder = Folder.Inbox,
    val starred: Boolean = false,
    val unread: Boolean = false,
    val labels: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val participants: List<String> = emptyList(),
)

data class Note(
    val id: Int,
    val title: String,
    val edited: String,
    val tags: List<String>,
    val paragraphs: List<String>,
    val checklist: List<Pair<String, Boolean>> = emptyList(),
    val outline: List<String> = emptyList(),
)

/** Made-up messages and notes. Every person and company here is fictional. */
object SampleData {
    val emails: List<Email> = listOf(
        Email(
            id = 1,
            sender = "Maya Lindqvist",
            senderAddress = "maya@northwind.example",
            subject = "Quarterly roadmap review",
            preview = "Here is the agenda for Thursday. I moved the tablet work to the top so we can",
            body = listOf(
                "Hi team,",
                "Here is the agenda for Thursday's roadmap review. I moved the tablet and foldable work to the top so we have enough time for it: the new list-detail inbox, the supporting pane for notes and the hinge-aware reader.",
                "Please skim the attached slides before the meeting and add your questions to the doc. Daniel will demo the book posture build on the foldable, and Priya has numbers from the beta on how people use the app on large screens.",
                "If anything else should be on the agenda, reply here by Wednesday noon.",
                "Thanks,\nMaya",
            ),
            time = "9:41",
            starred = true,
            unread = true,
            labels = listOf("Work", "Roadmap"),
            attachments = listOf(Attachment("Roadmap Q4.pdf", "2.4 MB"), Attachment("Beta metrics.csv", "86 KB")),
            participants = listOf("Maya Lindqvist", "Daniel Okafor", "Priya Raman", "You"),
        ),
        Email(
            id = 2,
            sender = "Daniel Okafor",
            senderAddress = "daniel@northwind.example",
            subject = "Foldable build is ready",
            preview = "The book posture build is on the internal track. Open a message and fold the",
            body = listOf(
                "The book posture build is on the internal track.",
                "Open a message and fold the device halfway: the list stays on the left page and the message moves to the right page, exactly at the hinge.",
            ),
            time = "8:15",
            unread = true,
            labels = listOf("Work"),
            participants = listOf("Daniel Okafor", "You"),
        ),
        Email(
            id = 3,
            sender = "Lisbon Stays",
            senderAddress = "bookings@lisbonstays.example",
            subject = "Your booking is confirmed",
            preview = "Rua das Flores 28, 3 nights from 14 November. Check-in from 3 pm, and the",
            body = listOf(
                "Your stay at Rua das Flores 28 is confirmed for 3 nights from 14 November.",
                "Check-in is from 3 pm. The host will send the door code the day before you arrive.",
            ),
            time = "Yesterday",
            starred = true,
            labels = listOf("Travel"),
            attachments = listOf(Attachment("Booking.pdf", "310 KB")),
            participants = listOf("Lisbon Stays", "You"),
        ),
        Email(
            id = 4,
            sender = "Priya Raman",
            senderAddress = "priya@northwind.example",
            subject = "Large screen usage, first numbers",
            preview = "Tablet sessions are up 38% since the two pane layout shipped, and people open",
            body = listOf(
                "Tablet sessions are up 38% since the two pane layout shipped, and people open twice as many messages per session.",
                "Full report on Thursday.",
            ),
            time = "Yesterday",
            labels = listOf("Work", "Data"),
            participants = listOf("Priya Raman", "Maya Lindqvist", "You"),
        ),
        Email(
            id = 5,
            sender = "Ceramics Studio",
            senderAddress = "hello@claystudio.example",
            subject = "Wheel class moved to Saturday",
            preview = "This week's wheel throwing class moves to Saturday at 10. Bring an apron, we",
            body = listOf("This week's wheel throwing class moves to Saturday at 10. Bring an apron, we will glaze last week's bowls."),
            time = "Mon",
            labels = listOf("Personal"),
            participants = listOf("Ceramics Studio", "You"),
        ),
        Email(
            id = 6,
            sender = "Tomás Herrera",
            senderAddress = "tomas@mail.example",
            subject = "Photos from the hike",
            preview = "Finally uploaded them. The one at the ridge with the fog is my favorite, I",
            body = listOf("Finally uploaded them. The one at the ridge with the fog is my favorite."),
            time = "Sun",
            starred = true,
            labels = listOf("Personal"),
            attachments = listOf(Attachment("Ridge.jpg", "4.1 MB"), Attachment("Lake.jpg", "3.8 MB")),
            participants = listOf("Tomás Herrera", "You"),
        ),
        Email(
            id = 7,
            sender = "Northwind IT",
            senderAddress = "it@northwind.example",
            subject = "New devices for the design team",
            preview = "The tablets and foldables for testing have arrived. Pick them up at the help",
            body = listOf("The tablets and foldables for testing have arrived. Pick them up at the help desk."),
            time = "Sat",
            labels = listOf("Work"),
            participants = listOf("Northwind IT", "You"),
        ),
        Email(
            id = 8,
            sender = "You",
            senderAddress = "you@northwind.example",
            subject = "Re: Design review notes",
            preview = "Thanks for the notes. I updated the spacing between panes to 12 dp and",
            body = listOf("Thanks for the notes. I updated the spacing between panes to 12 dp and moved the rail header."),
            time = "Fri",
            folder = Folder.Sent,
            labels = listOf("Work"),
            participants = listOf("Maya Lindqvist", "You"),
        ),
        Email(
            id = 9,
            sender = "You",
            senderAddress = "you@northwind.example",
            subject = "Dinner on Friday?",
            preview = "There is a new place near the river that does grilled fish, want to try it",
            body = listOf("There is a new place near the river that does grilled fish, want to try it on Friday?"),
            time = "Thu",
            folder = Folder.Sent,
            participants = listOf("Tomás Herrera", "You"),
        ),
    )

    fun emailsFor(section: String): List<Email> = when (section) {
        Sections.STARRED -> emails.filter { it.starred }
        Sections.SENT -> emails.filter { it.folder == Folder.Sent }
        else -> emails.filter { it.folder == Folder.Inbox }
    }

    val notes: List<Note> = listOf(
        Note(
            id = 1,
            title = "Lisbon trip plan",
            edited = "Edited today, 9:30",
            tags = listOf("Travel", "November"),
            paragraphs = listOf(
                "Three nights in Alfama, 14 to 17 November. The apartment is on Rua das Flores, ten minutes on foot from the river.",
                "Day one is for walking: up to the castle in the morning, lunch at the market, then the tram down to Belém before sunset. Day two is a train to Sintra; go early to beat the crowds at the palace.",
                "Keep the last evening free for fado. Ask the host for a small place, not the big ones on the main street.",
            ),
            checklist = listOf(
                "Book the Sintra train" to true,
                "Reserve dinner for Saturday" to false,
                "Download offline maps" to true,
                "Pack the rain jacket" to false,
            ),
            outline = listOf("Where we stay", "Day one: the city", "Day two: Sintra", "Last evening", "Checklist"),
        ),
        Note(
            id = 2,
            title = "Restaurants to try",
            edited = "Edited yesterday",
            tags = listOf("Travel", "Food"),
            paragraphs = listOf("Grilled sardines near the river, the bakery with custard tarts in Belém, and a fado house in Alfama."),
        ),
        Note(
            id = 3,
            title = "Packing list",
            edited = "Edited Monday",
            tags = listOf("Travel"),
            paragraphs = listOf("Rain jacket, comfortable shoes, the small camera, chargers and the travel adapter."),
        ),
        Note(
            id = 4,
            title = "Roadmap questions",
            edited = "Edited Sunday",
            tags = listOf("Work"),
            paragraphs = listOf("How do we test the hinge layout without a device? Which screens need a supporting pane first?"),
        ),
        Note(
            id = 5,
            title = "Book club: November",
            edited = "Edited last week",
            tags = listOf("Personal"),
            paragraphs = listOf("Read up to chapter twelve. Bring the question about the lighthouse keeper."),
        ),
    )

    private val avatarColors = listOf(
        Color(0xFF00897B),
        Color(0xFF5C6BC0),
        Color(0xFFEF6C00),
        Color(0xFFD81B60),
        Color(0xFF7CB342),
        Color(0xFF8E24AA),
        Color(0xFF039BE5),
    )

    fun avatarColor(name: String): Color = avatarColors[(name.hashCode() and Int.MAX_VALUE) % avatarColors.size]

    fun initials(name: String): String =
        name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}

object Sections {
    const val INBOX = "inbox"
    const val STARRED = "starred"
    const val SENT = "sent"
    const val NOTES = "notes"
}
