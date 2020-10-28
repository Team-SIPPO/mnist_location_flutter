import 'dart:async';
import 'dart:io';
import 'dart:ui';
import 'package:flutter/services.dart';

class LocationManager {

  static const MethodChannel _channel = MethodChannel('location_plugin');
  static const MethodChannel _background = MethodChannel('location_plugin_background');
  LocationManager(){
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

  Future<void> registerLocation(
      String name, Function(Map) callback) async {

    final List<dynamic> args = <dynamic>[
      PluginUtilities.getCallbackHandle(callback).toRawHandle()
    ];
    // final CallbackHandle callbackHandle = PluginUtilities.getCallbackHandle(callback);
    args.add(name);
    await _channel.invokeMethod('LocationPlugin.registerLocation', args);
    // await _background.invokeMethod('', args);
  }

}