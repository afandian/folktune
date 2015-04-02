# Folktune.org

A cloud-based service for you to store and share your tunes and find other people's.

## Folktune is

* a place to show off your tunes
* a place to to store your tunes in private
* a backup of your personal tune library
* an API to connect to various apps
* an online app where you can store, edit and download your tunes
* available on your desktop computer
* available on your phone
* a place to browse other peoples' tunebooks

## Components

### FolkTune Service + API

An central service that stores all the tunes. It has an API, which Apps use to connect.

### Web app

With the web app you can log into manage your tune books. You can also view other peoples' tune books.

### Desktop Sync

A program that lives on your Windows or Mac computer. This will sync your local ABC tune library up to the cloud.

### Mobile app

A mobile browsing app. Will have broadly the same features as the web app.


## Nuts and bolts

## Terminology

* **Document** a text file containing one or more ABC tunes separated by white space. No extra stuff.
* **Tune** a single ABC tune
* **Public** a Document that is Public is available to the world to browse and download
* **Private** a Document that has not been published
* **User** a Mozilla Persona user. For some purposes, an app may have its own user to represent 'anon'.

### Authentication

Authentication will be with Mozilla Persona. This is a trusted service run by the non-profit Mozilla. It handles security, password storage etc. The app will interact with Persona to validate the user, send the authorization to the Service, which will return a Authentication Token. This is a cookie. Authentication tokens last for a long time.

When the Desktop App authenticates, it will store the cookie in a specified place, e.g. `«user home»/folktunecookie` (whatever is OS appropriate). It will then send the cookie in a header with every request to the API.

Authentication tokens may last forever, but they may be revoked. If the response from the API `403 Forbidden`, the app must re-authenticate.

For web apps, this is all automatic.

### Documents

A Document always belongs to a user. An App can upload, download, delete, change the metadata a document on behalf of a user.

#### Identifiers

A Document has an owner and an identifier. It must have an identifier in order for the service to keep documents updated, allow retrieval and deletion. The identifier of the document is a string, of alphanumeric characters and slashes. It is up to the App in question to supply an identifier when it uploads a Document. Identifiers exist in a per-user namespace, so two users may have documents with  the same identifier.

Slashes can be included in identifiers to represent a directory structure. However, it is up to the App to interpret them. A given app might represent a set of identifiers with slashes in as a directory structure, or just a simple list of identifiers with slashes in them.

##### Relating to Desktop sync

Desktop Sync app will probably assign idenfiers based on the the full path relative to the 'watched directory'. I suggest normalised to use `/` path separators (BTW Windows is able to handle these transparently I think). e.g. `myTunes/compositions/airs.abc`. This makes cross-platform (android, web URLs) possible.

Document identifiers are persistent and may not be changed. When a file is moved in the desktop file system, the Desktop App can  send a 'delete' request to the old identifier and a 'create' to the new one.

> We could also perhaps store redirects to indicate that a file moves?
> How much do we care about capturing and representing the moving of a file? 
> API could detect when a file is uploaded under a new identifier that has the same content as a deleted file and set up a redirect?

##### Relating to web app

The Web App will allow Users to create Documents online. It will support navigation of the same directory-structure-like paths. 

> **ALTERNATIVELY** no directories at all. Would make life much simpler.


#### Metdata

Every tune has the following metadata:

* public?


### Apps

Every app will communicate with the service via the API. Most apps will perform an action on behalf of a user, and will bear their authentication token. Apps that we will launch with:

* web app
* desktop sync client

Apps do not always perform actions on behalf of a user. For example, they may 

* browse and download public Documents


### API

For most users, API only ever speaks in terms of Documents. If the user wants to upload a single ABC tune, it's in the form of a Document containing one one.

* published status
* identifier

If a user uploads a Document as Public, anyone may discover and view that Document. The Tunes in those Documents are also Public.

The following endpoints exist:

* `folktune.org/api/users/«user_id»/documents/`
	* `GET` all Document Identifiers belonging to User. 
* `folktune.org/api/users/«user_id»/documents/«document_id»`
	* `POST` or `PUT` new or existing Document with identifier*
	* `DELETE` Document with identifier*
	* `GET` Document with identifier*
* `folktune.org/api/documents`
	*  list of all Documents
* `folktune.org/api/users/`
	* list of all users with at least 1 public Document

		
##### Authentication

Every authenticated request must contain the `Cookie:` header, containing the authorization cookie. Every endpoint may return `403 Forbidden`, in which case the application should reauthenticate and try again.

The precise nature of the «user_id» isn't determined. Probably an increasing numerical ID. It's not an email address because they can change (and Persona can link addresses), and for privacy.

#### Content and structure

The  `document` endpoint deals in the literal ABC files. The text of the ABC file should be posted, in UTF-8. Extra headers may be supplied:

    X-Folktune-Secrecy: Private|Public
    
This is assumed to be public if not supplied (users can log in to the web interface at a later date).

The `documents` endpoint returns a list of identifiers in plain text, one per line.

#### Sync

The `Last-Modified` date is set for all tunes (RFC 7231). If it is supplied during an upload, that date is stored. If not, the upload date is used if there were any changes, or if there aren't, the date isn't changed. 

The sync software should get the list of Documents from the Documents endpoint, then scan each (performing a GET or HEAD request), and decide what to do on that basis.

### Internals

The Document that is downloaded is identical to the Document that was uploaded. However, internally, it is split into Tunes. Tunes are content-addressed (by their hashcode). If two Users upload two Documents containing an identical Tune, internal both documents will reference the same Tune internally. 

For exploratory purposes, the API will expose the Tune ID (probably a hashcode). This will be publicly accessible *iff* there is at least one Public Document containing it (i.e. it can be reached through a public Document).

#### Example

Jim uploads a Document from his computer with `The Miller's Delight` and `The Lovely Rolling Fields`. It's stored as `C:\My Documents\abc\english\dreadfultunes.abc`. The Desktop Sync app then posts the following:

    POST: folktune.org/api/users/100/documents/english/dreadfultunes.abc
   	Cookie: whatevs
    X-Folktune-Secrecy: Public
    
    X:0
    T: The Miller's Delight
    K: C
    CDEFG
    
    X:1
    T: The Lovely Rolling Fields
    K: G
    GGGGG
    
The FolkTuneFinder Export app now pushes in the second tune, with its own ID scheme

    POST: folktune.org/api/users/999/documents/folktunefinder.com/tunes/8888
   	Cookie: whatevs
    X-Folktune-Secrecy: Public
        
    X:1
    T: The Lovely Rolling Fields
    K: G
    GGGGG

Internally now there are 2 tunes and 2 documents.

A user may visit the web app at (for example) `folktune.org/users/100/documents/english/dreadfultunes.abc` and get a nice view of the short tunebook. Next to each one is a `this tune in other collections` button, which takes the user to the view backed by `/api/tunes/d41d8cd98f00b204e9800998ecf8427e`. It displays:

* this tune is in:
	* tunebook `/api/users/100/documents/english/dreadfultunes.abc`
	* tunebook `/api/users/999/documents/folktunefinder.com/tunes/8888`
 
 If Jim then edits his secrecy to be private, then that screen still works because another user, number 999 (which is the FolkTuneFinder anon user) also uploaded it. 

## Whys and Wherefores

### Objectionable Content

Users may post objectionable content:

* in cases where someone has made a copyright claim
* where non-ABC junk or undesirable material is posted in bad faith

The serivce reserves the right to:

* make a given document private, and lock it private
* delete a document
* delete a user account

If any material is deleted, a snapshot of all active documents will be emailed to the user first.

### Archival

Users may download an archive of all their content at any point. There will be an option to email a ZIP file of all the user's content every month that can be set in the web interface. 

If the service shuts down at any point in the future, we undertake to email every archive to the owner of it.