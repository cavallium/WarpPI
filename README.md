# WarpPI Calculator
Step-by-step algebra calculator for Raspberry PI.

**This project is experimental and strictly related to my calculator, designed to run on a Raspberry Pi.**<br>
The Java 21 hardware build requires an ARMv7/ARMv8 board (Raspberry Pi Zero 2/W or newer); the ARMv6 Zero 1/W is not supported. On Zero 2, use Java 21.0.8 or newer. GPIO access uses Pi4J's GpioD provider and requires Linux 6.1.21 or newer on Bullseye, or 6.6.22 or newer on Bookworm.
**Many fundamental features are missing, this project is still in its infancy.**<br>

**If you really want to build and test it on your computer take account of that.**

# Live preview
This preview has been built using TeaVM: a JVM made in javascript.
Obviously it runs slower than the original.
https://local.cavallium.it/calculator/

# Keypad PCB
https://workspace.circuitmaker.com/Projects/Details/Andrea-Cavalli/Scientific-calculator-Keypad
![alt text](https://raw.githubusercontent.com/Cavallium/WarpPI/master/core/src/main/resources/keypad-preview.jpg "Pictures")

# Photos and pictures
![alt text](https://raw.githubusercontent.com/Cavallium/WarpPI/master/core/src/main/resources/algebra_input.gif "Algebra input screen")
![alt text](https://raw.githubusercontent.com/Cavallium/WarpPI/master/core/src/main/resources/algebra%20variable%20type.gif "Algebra variable type menu")
![alt text](https://raw.githubusercontent.com/Cavallium/WarpPI/master/core/src/main/resources/decimal.png "Example expression")
![alt text](https://raw.githubusercontent.com/Cavallium/WarpPI/master/core/src/main/resources/algebra.png "Simplification of an expression")
