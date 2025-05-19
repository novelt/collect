- changed application id to be Novel-T specific (ch.novelt.gts.odk, be careful we removed the `.org` segment)

- modified:
`<category android:name="android.intent.category.LAUNCHER" />`
to
`<category android:name="android.intent.category.LEANBACK_LAUNCHER" />`
to avoid adding GTS Collect icon to launcher and manually launch it (+ removed LeakCanary also to avoid adding icon to launcher)

- flavors (for different envs) (with applicationId suffix)

- content provider's authority using applicationId (+ contracts)

- shared Dagger setup: to allow calls from GTS Tracker to content providers even if GTS Collect has not been launched yet. Otherwise it crashes as Dagger is not yet initialized

- opened and made static, the method `LocalFormUseCases.parseForm` (a method used to parse a form file and determine metadata, used in auto sync of forms folder `/forms`)

- opened to inheritance `FormUriActivity` to allow us to override which applications can respond to GTS Tracker by setting `ACTION` to the applicationId (created `GtsFormUriActivity`, with also discarding drafts, default map basemap...).

- modified `ZipUtils` to allow subfolder (like `media`) to be written correctly rather than creating specific method. Used when installing a form as a `.zip` file.

- hide a panel regarding finalized form displayed at the final step
