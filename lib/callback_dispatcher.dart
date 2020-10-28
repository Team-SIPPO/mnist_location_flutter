

import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void callbackDispatcher() {
  const MethodChannel _background = MethodChannel('location_plugin_background');
  WidgetsFlutterBinding.ensureInitialized();

  _background.setMethodCallHandler((call) async {
    Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(call.arguments[0])
    );
    print("MethodChannel dart");
    Map value = call.arguments[1];
    callback(value);
    print("MethodChannel dart called");
  });
}
