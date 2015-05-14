/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2015 Aimluck,Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Default container configuration. To change the configuration, you have two options:
//
// A. If you run the Java server: Create your own "myContainer.js" file and
// modify the value in web.xml.
//
//  B. If you run the PHP server: Create a myContainer.js, copy the contents of container.js to it,
//  change
//		{"gadgets.container" : ["default"],
//  to
//		{"gadgets.container" : ["myContainer"],
// And make your changes that you need to myContainer.js.
// Just make sure on the iframe URL you specify &container=myContainer
// for it to use that config.
//
// All configurations will automatically inherit values from this
// config, so you only need to provide configuration for items
// that you require explicit special casing for.
//
// Please namespace your attributes using the same conventions
// as you would for javascript objects, e.g. gadgets.features
// rather than "features".

// NOTE: Please _don't_ leave trailing commas because the php json parser
// errors out on this.

// Container must be an array; this allows multiple containers
// to share configuration.

// Note that you can embed values directly or you can choose to have values read from a file on disk
// or read from the classpath ("foo-key" : "file:///foo-file.txt" || "foo-key" : "res://foo-file.txt")
// TODO: Move out accel container config into a separate accel.js file.
{"gadgets.container" : ["default"],

// Set of regular expressions to validate the parent parameter. This is
// necessary to support situations where you want a single container to support
// multiple possible host names (such as for localized domains, such as
// <language>.example.org. If left as null, the parent parameter will be
// ignored; otherwise, any requests that do not include a parent
// value matching this set will return a 404 error.
"gadgets.parent" : null,

// Origins for CORS requests and/or Referer validation
// Indicate a set of origins or an entry with * to indicate that all origins are allowed
"gadgets.parentOrigins" : ["*"],

// Various urls generated throughout the code base.
// iframeBaseUri will automatically have the host inserted
// if locked domain is enabled and the implementation supports it.
// query parameters will be added.
"gadgets.iframeBaseUri" : "${CONTEXT_ROOT}/gadgets/ifr",
"gadgets.uri.iframe.basePath" : "${CONTEXT_ROOT}/gadgets/ifr",

// Callback URL.  Scheme relative URL for easy switch between https/http.
"gadgets.uri.oauth.callbackTemplate" : "//%host%${CONTEXT_ROOT}/gadgets/oauthcallback",

// Config param to load Opensocial data for social
// preloads in data pipelining.  %host% will be
// substituted with the current host.
"gadgets.osDataUri" : "//%host%${CONTEXT_ROOT}/rpc",

// Use an insecure security token by default
"gadgets.securityTokenType" : "secure",
"gadgets.securityTokenKey" : "res://aipo/securityTokenKey.txt",

// Uncomment the securityTokenType and one of the securityTokenKey's to switch to a secure version.
// Note that you can choose to use an embedded key, a filesystem reference or a classpath reference.
// The best way to generate a key is to do something like this:
// dd if=/dev/random bs=32 count=1 | openssl base64
//
//"gadgets.securityTokenType" : "secure",
//"gadgets.securityTokenKey" : "default-insecure-embedded-key",
//"gadgets.securityTokenKey" : "file:///path/to/key/file.txt",
//"gadgets.securityTokenKey" : "res://some-file-on-the-classpath.txt",

// OS 2.0 Gadget DOCTYPE: used in Gadgets with @specificationVersion 2.0 or greater and
// quirksmode on Gadget has not been set.
"gadgets.doctype_qname" : "HTML",  //HTML5 doctype
"gadgets.doctype_pubid" : "",
"gadgets.doctype_sysid" : "",

// In a locked domain config, these can remain as-is in order to have requests encountered use the
// host they came in on (locked host).
"default.domain.locked.client" : "%host%",
"default.domain.locked.server" : "%authority%",

// IMPORTANT: EDITME: In a locked domain configuration, these should be changed to explicit values of
// your unlocked host. You should not use %host% or %authority% replacements or these defaults in a
// locked domain deployment.
// Both of these values will likely be identical in a real locked domain deployment.
"default.domain.unlocked.client" : "${Cur['default.domain.locked.client']}",
"default.domain.unlocked.server" : "${Cur['default.domain.locked.server']}",

// You can change this if you wish unlocked gadgets to render on a different domain from the default.
"gadgets.uri.iframe.unlockedDomain" : "${Cur['default.domain.unlocked.server']}", // DNS domain on which *unlocked* gadgets should render.

// IMPORTANT: EDITME: In a locked domain configuration, this suffix should be provided explicitly.
// It is recommended that it be a separate top-level-domain (TLD) than the unlocked TLD.
// You should not use replacement here (avoid %authority%)
// Example: unlockedDomain="shindig.example.com" lockedDomainSuffix="-locked.example-gadgets.com"
"gadgets.uri.iframe.lockedDomainSuffix" : "${Cur['default.domain.locked.server']}", // DNS domain on which *locked* gadgets should render.

// Should all gadgets be forced on to a locked domain?
"gadgets.uri.iframe.lockedDomainRequired" : true,

// The permitted domain where the render request is sent from. For examle: ["www.hostA.com", "www.hostB.com"]
// Empty means all domains are permitted.
"shindig.locked-domain.permittedRefererDomains" : [],

// Default Js Uri config: also must be overridden.
// gadgets.uri.js.host should be protocol relative.
"gadgets.uri.js.host" : "//${Cur['default.domain.unlocked.server']}", // Use unlocked host for better caching.

// If you change the js.path you will need to define window.__CONTAINER_SCRIPT_ID prior to loading the <script>
// tag for container JavaScript into the DOM.
"gadgets.uri.js.path" : "${CONTEXT_ROOT}/gadgets/js",

// Default concat Uri config; used for testing.
"gadgets.uri.concat.host" : "${Cur['default.domain.unlocked.server']}", // Use unlocked host for better caching.
"gadgets.uri.concat.path" : "${CONTEXT_ROOT}/gadgets/concat",
"gadgets.uri.concat.js.splitToken" : "false",

// Default proxy Uri config; used for testing.
"gadgets.uri.proxy.host" : "${Cur['default.domain.unlocked.server']}", // Use unlocked host for better caching.
"gadgets.uri.proxy.path" : "${CONTEXT_ROOT}/gadgets/proxy",

// Enables/Disables feature administration
"gadgets.admin.enableFeatureAdministration" : false,

// Enables whitelist checks
"gadgets.admin.enableGadgetWhitelist" : false,

// Max post size for posts through the makeRequest proxy.
"gadgets.jsonProxyUrl.maxPostSize" : 5242880, // 5 MiB

// This config data will be passed down to javascript. Please
// configure your object using the feature name rather than
// the javascript name.

// Only configuration for required features will be used.
// See individual feature.xml files for configuration details.
"gadgets.features" : {
  "core.io" : {
    // Note: ${Cur['gadgets.uri.proxy.path']} is an open proxy. Be careful how you expose this!
    // Note: These urls should be protocol relative (start with //)
    "proxyUrl" : "//${Cur['default.domain.unlocked.client']}${Cur['gadgets.uri.proxy.path']}%filename%?container=%container%&refresh=%refresh%&url=%url%%authz%%rewriteMime%",
    "jsonProxyUrl" : "//${Cur['default.domain.locked.client']}${CONTEXT_ROOT}/gadgets/makeRequest",
    // Note: this setting MUST be supplied in every container config object, as there is no default if it is not supplied.
    "unparseableCruft" : "throw 1; < don't be evil' >",

    // This variable is needed during the config init to parse config augmentation
    "jsPath" : "${Cur['gadgets.uri.js.path']}",

    // interval in milliseconds used to poll xhr request for the readyState
    "xhrPollIntervalMs" : 50
  },
  "views" : {
    "home" : {
      "isOnlyVisible" : false,
      "aliases": ["default"]
    },
    "preview" : {
        "isOnlyVisible" : false
    },
    "canvas" : {
      "isOnlyVisible" : true
    },
    "popup" : {
        "isOnlyVisible" : true
    }
  },
  "tabs": {
    "css" : [
      ".tablib_table {",
      "margin:5px 0;",
      "width: 100%;",
      "border-collapse: separate;",
      "border-spacing: 0px;",
      "empty-cells: show;",
      "text-align: center;",
      "font-size: .9em;",
    "}",
    ".tablib_emptyTab {",
      "border-bottom: 1px solid #676767;",
      "padding: 0px 1px;",
    "}",
    ".tablib_spacerTab {",
      "border-bottom: 1px solid #676767;",
      "padding: 0px 1px;",
      "width: 1px;",
    "}",
    ".tablib_selected {",
      "padding: 2px 0px;",
      "background-color: #ffffff;",
      "border: 1px solid #A0A0A0;",
      "border-bottom-width: 0px;",
      "font-weight: bold;",
      "width: 100px;",
      "cursor: default;",
    "}",
    ".tablib_unselected {",
      "padding: 2px 0px;",
      "background-color: #EEE;",
      "border: 1px solid #cccccc;",
      "border-bottom-color: #A0A0A0;",
      "width: 100px;",
      "cursor: pointer;",
    "}",
    ".tablib_navContainer {",
      "width: 10px;",
      "vertical-align: middle;",
    "}",
    ".tablib_navContainer a:link, ",
    ".tablib_navContainer a:visited, ",
    ".tablib_navContainer a:hover {",
      "color: #3366aa;",
      "text-decoration: none;",
    "}",
    ]
  },
  "minimessage": {
    "css": [
        ".mmlib_table {",
        "width:100%;",
        "margin: 5px 0;",
        "-webkit-border-radius: 4px;",
        "-moz-border-radius: 4px;",
        "-ms-border-radius: 4px;",
		"border-radius: 4px;",
		"background: #f0f0f0;",
      "}",
      ".mmlib_table td {",
      "padding: 3px 5px;",
      "}",
      ".mmlib_xlink {",
        "display:block;",
        "cursor: pointer;",
      "}"
     ]
  },
  "rpc" : {
    // Path to the relay file. Automatically appended to the parent
    // parameter if it passes input validation and is not null.
    // This should never be on the same host in a production environment!
    // Only use this for TESTING!
    "parentRelayUrl" : "/container/gadgets/files/container/rpc_relay.html",

    // If true, this will use the legacy ifpc wire format when making rpc
    // requests.
    "useLegacyProtocol" : false,

    // Path to the cross-domain enabling SWF for rpc's Flash transport.
    "commSwf": "/xpc.swf",
    "passReferrer": "c2p:query"
  },
  // Skin defaults
  "skins" : {
    "properties" : {
      "BG_COLOR": "",
      "BG_IMAGE": "",
      "BG_POSITION": "",
      "BG_REPEAT": "",
      "FONT_COLOR": "#666666",
      "ANCHOR_COLOR": "#666666"
    }
  },
  "opensocial" : {
    // Path to fetch opensocial data from
    // Must be on the same domain as the gadget rendering server
    "path" : "//%host%${CONTEXT_ROOT}/rpc",
    // Path to issue invalidate calls
    "invalidatePath" : "//%host%${CONTEXT_ROOT}/rpc",
    "domain" : "localhost",
    "enableCaja" : false,
    "supportedFields" : {
       "person" : ["id", {"name" : ["familyName", "givenName"]}, "displayName"],
       "group" : ["id", "title", "type"],
       "activity" : ["appId", "externalId", "id", "priority", "title", "recipients"];
    }
  },
  "osapi.services" : {
    // Specifying a binding to "container.listMethods" instructs osapi to dynamicaly introspect the services
    // provided by the container and delay the gadget onLoad handler until that introspection is
    // complete.
    // Alternatively a container can directly configure services here rather than having them
    // introspected. Simply list out the available servies and omit "container.listMethods" to
    // avoid the initialization delay caused by gadgets.rpc
    // E.g. "gadgets.rpc" : ["activities.requestCreate", "messages.requestSend", "requestShareApp", "requestPermission"]
    "gadgets.rpc" : [ "//%host%${CONTEXT_ROOT}/rpc" ]
  },
  "osapi" : {
    // The endpoints to query for available JSONRPC/REST services
    "endPoints" : [ "//%host%${CONTEXT_ROOT}/rpc" ]
  },
  "osml": {
    // OSML library resource.  Can be set to null or the empty string to disable OSML
    // for a container.
    "library": "config/OSML_library.xml"
  },
  "shindig-container": {
    "serverBase": "${CONTEXT_ROOT}/gadgets/"
  },
  "container" : {
    "relayPath": "${CONTEXT_ROOT}/gadgets/files/container/rpc_relay.html",

    //Enables/Disables the RPC arbitrator functionality in the common container
    "enableRpcArbitration": false,

    // This variable is needed during the container feature init.
    "jsPath" : "${Cur['gadgets.uri.js.path']}"
  }
}
}
