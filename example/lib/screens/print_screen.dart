// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'package:flutter/material.dart';
import 'package:panda_printer_example/controllers/printer_controller.dart';
import 'package:panda_printer_example/models/printer_model.dart';

class TestPrintScreen extends StatefulWidget {
  const TestPrintScreen({super.key});

  @override
  State<TestPrintScreen> createState() => _TestPrintScreenState();
}

class _TestPrintScreenState extends State<TestPrintScreen> {
  final PrinterController _controller = PrinterController();

  @override
  void initState() {
    _controller.init();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Printer test'),
      ),
      body: Row(
        children: [
          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                AnimatedBuilder(
                  animation: _controller,
                  builder: (_, __) {
                    if (_controller.connectedPrinter == null) {
                      return const SizedBox.shrink();
                    }

                    return Column(
                      children: [
                        Text('Connected printer: ${_controller.connectedPrinter?.name}'),
                      ],
                    );
                  },
                ),
                const SizedBox(height: 16),
                AnimatedBuilder(
                    animation: _controller,
                    builder: (_, __) {
                      if (!_controller.isInitCompleted) {
                        return const SizedBox.shrink();
                      }
                      return Row(
                        children: [
                          const SizedBox(width: 16),
                          MaterialButton(
                            onPressed: () {
                              _controller.lookUpPrinters();
                            },
                            color: Colors.purple[100],
                            child: const Text('Search for printers'),
                          ),
                          const SizedBox(width: 16),
                          MaterialButton(
                            onPressed: () {
                              _controller.print();
                            },
                            color: Colors.purple[100],
                            child: const Text('Print'),
                          ),
                        ],
                      );
                    }),
              ],
            ),
          ),
          Expanded(
            flex: 2,
            child: Center(
              child: PrintersList(controller: _controller),
            ),
          )
        ],
      ),
    );
  }
}

class PrintersList extends StatelessWidget {
  const PrintersList({
    super.key,
    required PrinterController controller,
  }) : _controller = controller;

  final PrinterController _controller;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: 8),
        const Text('Found printers, tap to connect'),
        const SizedBox(height: 8),
        Expanded(
          child: AnimatedBuilder(
              animation: _controller,
              builder: (_, __) {
                return ListView.separated(
                  itemBuilder: (context, index) {
                    PrinterModel item = _controller.foundPrinters[index];
                    return PrinterItem(
                      item: item,
                      onPressed: () {
                        if (_controller.connectedPrinter?.address != item.address) {
                          _controller.connectPrinter(item);
                        }
                      },
                    );
                  },
                  separatorBuilder: (context, index) {
                    return const SizedBox(height: 8);
                  },
                  itemCount: _controller.foundPrinters.length,
                );
              }),
        ),
      ],
    );
  }
}

class PrinterItem extends StatelessWidget {
  const PrinterItem({
    super.key,
    required this.item,
    required this.onPressed,
  });
  final PrinterModel item;
  final Function() onPressed;
  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onPressed,
      child: Container(
        padding: const EdgeInsets.symmetric(
          vertical: 8.0,
          horizontal: 16.0,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(item.name ?? 'NO_NAME'),
            Text(item.address),
          ],
        ),
      ),
    );
  }
}
