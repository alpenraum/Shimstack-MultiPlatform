import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.mainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(edges: .all)
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}


/*
 import UIKit
 import SwiftUI
 import ComposeApp

 struct ComposeView: UIViewControllerRepresentable {
     func makeUIViewController(context: Context) -> UIViewController {
         MainViewControllerKt.MainViewController()
     }

     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 }

 struct ContentView: View {
     @State var scaleAmount = 1.0
     @State var isHomeRootScreen = false
     var body: some View {
         ZStack {
             if isHomeRootScreen {
                 ComposeView()
                     .ignoresSafeArea(.keyboard)
             } else {
                 Image(.splashIcon)
                     .resizable()
                     .aspectRatio(contentMode: .fit)
                     .scaleEffect(scaleAmount)
                     .frame(width:60)
                     .onAppear() {
                         withAnimation(.easeIn(duration: 1)){
                             scaleAmount = 100.0
                         }

                         DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
                             isHomeRootScreen = true
                         })}
             }
         }
         .ignoresSafeArea(( isHomeRootScreen ? .keyboard : .all))
     }
 }




 */
