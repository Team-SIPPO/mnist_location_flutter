import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mnist_location_flutter/callback_dispatcher.dart';

class LocationManager {
  static const MethodChannel _channel = MethodChannel('location_plugin');
  static bool initialized = false;

  Future<void> registerLocation(
      String name, Function(Map) callback) async {

    if(!LocationManager.initialized){
      final List<dynamic> args = <dynamic>[
        PluginUtilities.getCallbackHandle(callbackDispatcher).toRawHandle()
      ];
      args.add(name);
      await _channel.invokeMethod('LocationPlugin.initialize', args);
      await Future.delayed(Duration(seconds: 5));
      LocationManager.initialized = true;
    }

    final List<dynamic> args = <dynamic>[
      PluginUtilities.getCallbackHandle(callback).toRawHandle()
    ];
    // final CallbackHandle callbackHandle = PluginUtilities.getCallbackHandle(callback);
    args.add(name);
    await _channel.invokeMethod('LocationPlugin.registerLocation', args);
    // await _background.invokeMethod('', args);
  }

}