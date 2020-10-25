import 'dart:async';
import 'dart:io';
import 'dart:ui';
import 'package:flutter/services.dart';

class LocationManager {

  static const MethodChannel _channel = MethodChannel('location_plugin');

  static Future<void> registerLocation(
      String name) async {
    await _channel.invokeMethod('LocationPlugin.registerLocation', [name]);
  }

}