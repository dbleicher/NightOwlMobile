## This repository contains the UI code for PocketWGU on Android.

### You'll notice that none of the networking code is here.  There's a reason for that...  
The networking parts of PocketWGU make use of a variety of API the author has discovered
over the years.  While these API are discoverable/accessible to students with proper credentials,
WGU has not chosen to publish these for general use, and so I didn't feel it would be appropriate
for me to do so.

With that said, there is a ton a messy, poorly-documented code within this repo that demonstrates
how I've accomplished everything else within this app.  As big and bloated as this codebase is, I
would still point out that the resulting app is quite a bit smaller, and in some ways faster, than
available alternatives.

If you are interested in trying to build this app yourself, you'll need three things:

  1. The networking library.  I am not publishing it (see above), but I would be willing to
  provide the AAR (binary) for folks who ask nicely and really want to try building the app.
  2. A build.gradle script for the app itself.  I have not included mine as it contains some
  sensitive info.  I can send you a stripped-down version if you'd like.
  3. Elbow grease:  ultimately, my build process includes the library project itself, so you'll
  need to figure out how to include the binary netlib as a dependency.  This shouldn't be too hard
  but I haven't tried it myself.

One final note...  While the source code published to this repository is available for your use
under a very permissive [license](https://github.com/dbleicher/NightOwlMobile/blob/Android-UI/LICENSE.md "License"), I retain the exclusive rights to the PocketWGU name and the app's
logo.  Do what you want with the code, but please don't use the name or logo in your projects. 

Enjoy!